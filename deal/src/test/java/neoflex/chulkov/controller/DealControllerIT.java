package neoflex.chulkov.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import jakarta.transaction.Transactional;
import neoflex.chulkov.DealApplication;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.dto.enums.CreditStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.repository.ClientRepository;
import neoflex.chulkov.repository.StatementRepository;
import neoflex.chulkov.service.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(classes = DealApplication.class)
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
public class DealControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    ClientService clientService;
    @Autowired
    StatementRepository statementRepository;

    @Test
    @DisplayName("Создается и сохраняется в бд клиент и заявление и насыщаются полями из request, возвращается список из 4 вариантов кредита")
    void statement_RequestIsValid_ReturnsListOf4LoanOfferDtoAndSaveClientAndStatement() throws Exception {
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/calculator/offers"))
                .withRequestBody(WireMock.equalToJson("""
                    {
                      "amount": 500000,
                      "term": 12,
                      "firstName": "Vlad",
                      "lastName": "Simonyan",
                      "middleName": "Igorevich",
                      "email": "arte2m@example.com",
                      "birthday": [1995, 3, 23], 
                      "passportSeries": "1234",
                      "passportNumber": "567890"
                    }
                    """, true, true)) // Обрати внимание на массив в birthday
                .willReturn(WireMock.ok("""
                    [
                        {
                            "requestedAmount": 500000,
                            "totalAmount": 500000,
                            "term": 12,
                            "monthlyPayment": 46317.25,
                            "rate": 20,
                            "isInsuranceEnabled": false,
                            "isSalaryClient": false
                        }
                    ]
                    """).withHeader("Content-Type", "application/json")));

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                            "amount": 500000,
                            "term": 12,
                            "firstName": "Vlad",
                            "lastName": "Simonyan",
                            "middleName": "Igorevich",
                            "email": "arte2m@example.com",
                            "birthday": "1995-03-23",
                            "passportSeries": "1234",
                            "passportNumber": "567890"
                            }
                            """)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated());

        var clients = clientRepository.findAll();
        var statements = statementRepository.findAll();
        assertEquals(1, clients.size());
        assertEquals(1, statements.size());

        var client = clients.get(0);
        var statement = statements.get(0);
        assertEquals(LocalDate.parse("1995-03-23"), client.getBirthDate());
        assertEquals("Vlad", client.getFirstName());
        assertEquals("Simonyan", client.getLastName());
        assertEquals("Igorevich", client.getMiddleName());
        assertEquals("arte2m@example.com", client.getEmail());
        assertEquals("1234", client.getPassport().getSeries());
        assertEquals("567890", client.getPassport().getNumber());
        assertEquals(client, statement.getClient());
    }

    @Test
    @DisplayName("Выбирается один из 4 LoanOffer и обновляется statement")
    void select_RequestIsValid_updateInformationInStatement() throws Exception {
        Client client = clientService.createClient(new LoanStatementRequestDto()
                .amount(BigDecimal.valueOf(50000))
                .term(12)
                .firstName("Vlad")
                .lastName("Simonyan")
                .middleName("Igorevich")
                .email("arte2m@example.com")
                .birthday(LocalDate.parse("1995-03-23"))
                .passportNumber("1234")
                .passportSeries("567890"));
        Statement statement = new Statement();
        statement.setClient(client);
        statement = statementRepository.save(statement);
        UUID validStatementId = statement.getStatementId();
        mockMvc.perform(MockMvcRequestBuilders.post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                         "statementId": "%s",
                                         "requestedAmount": 500000,
                                         "totalAmount": 500000,
                                         "term": 12,
                                         "monthlyPayment": 46317.25,
                                         "rate": 20,
                                         "isInsuranceEnabled": false,
                                         "isSalaryClient": false
                                     }
                                """.formatted(validStatementId.toString()))
                )
                .andExpect(MockMvcResultMatchers.status().isAccepted());

        var statements = statementRepository.findAll();
        assertEquals(1, statements.size());
        var updatedStatement = statements.get(0);
        assertEquals(1, updatedStatement.getStatusHistory().size());
        assertEquals(ApplicationStatus.APPROVED, updatedStatement.getStatusHistory().get(0).getStatus());
        assertEquals(ApplicationStatus.APPROVED, updatedStatement.getStatus());
    }

    @Test
    @DisplayName("Насыщает информацию клиента, обновляет заявление и сохраняет в бд ифнормацию о кредите")
    void calculateCredit_RequestIsValid_ShouldUpdateClientAndStatementAndSaveCreditInformation() throws Exception {
        Client client = clientService.createClient(new LoanStatementRequestDto()
                        .amount(BigDecimal.valueOf(50000))
                        .term(12)
                        .firstName("Vlad")
                        .lastName("Simonyan")
                        .middleName("Igorevich")
                        .email("arte2m@example.com")
                        .birthday(LocalDate.parse("1995-03-23"))
                        .passportNumber("1234")
                        .passportSeries("567890"));
        Statement statement = new Statement();
        statement.setClient(client);
        statement = statementRepository.save(statement);
        UUID validStatementId = statement.getStatementId();
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/calculator/calc"))
                .withRequestBody(WireMock.containing("\"passportIssueDate\":[2015,3,23]")) // Если проверяешь тело
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "amount": 500000,
                                "term": 12,
                                "monthlyPayment": 46317.25,
                                "rate": 20,
                                "psk": 20.5,
                                "isInsuranceEnabled": false,
                                "isSalaryClient": false,
                                "paymentSchedule": []
                            }
                            """)));

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/calculate/{statementId}", validStatementId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "gender": "MALE",
                        "maritalStatus": "SINGLE",
                        "dependentAmount": 0,
                        "passportIssueDate": "2015-03-23",
                        "passportIssueBranch": "123-456",
                        "employment": {
                            "employmentStatus": "EMPLOYED",
                            "employerINN": "123456789012",
                            "salary": 100000,
                            "position": "WORKER",
                            "workExperienceTotal": 60,
                            "workExperienceCurrent": 24
                        },
                        "accountNumber": "40817810099910004312"
                    }
                    """)).andExpect(MockMvcResultMatchers.status().isAccepted());
        Statement updatedStatement = statementRepository.findById(validStatementId).orElseThrow();
        Client updatedClient = updatedStatement.getClient();
        assertEquals("123-456", updatedClient.getPassport().getIssueBranch());
        assertEquals("40817810099910004312", updatedClient.getAccountNumber());
        assertNotNull(updatedClient.getEmployment(), "Данные о работе должны быть сохранены");
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(updatedClient.getEmployment().getSalary()));
        assertEquals(ApplicationStatus.CC_APPROVED, updatedStatement.getStatus(), "Статус должен быть CC_APPROVED");
        assertNotNull(updatedStatement.getCredit(), "Кредит должен быть создан и привязан к заявке");
        assertEquals(CreditStatus.CALCULATED, updatedStatement.getCredit().getCreditStatus());
        assertEquals(0, BigDecimal.valueOf(500000).compareTo(updatedStatement.getCredit().getAmount()));
    }

    @Test
    @Transactional
    @DisplayName("Полный цикл кредитования: Создание заявки -> Выбор оффера -> Финальный расчет")
    void fullCreditLifecycle_ShouldPassSuccessfully() throws Exception {
        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/calculator/offers"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                        "requestedAmount": 500000,
                                        "totalAmount": 550000.00,
                                        "term": 12,
                                        "monthlyPayment": 49382.91,
                                        "rate": 14,
                                        "isInsuranceEnabled": true,
                                        "isSalaryClient": true
                                    }
                                ]
                                """)));

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/calculator/calc"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "amount": 500000,
                                    "term": 12,
                                    "monthlyPayment": 49382.91,
                                    "rate": 14,
                                    "psk": 14.5,
                                    "isInsuranceEnabled": true,
                                    "isSalaryClient": true,
                                    "paymentSchedule": []
                                }
                                """)));

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "amount": 500000,
                                "term": 12,
                                "firstName": "Vlad",
                                "lastName": "Simonyan",
                                "middleName": "Igorevich",
                                "email": "arte2m@example.com",
                                "birthday": "1995-03-23",
                                "passportSeries": "1234",
                                "passportNumber": "567890"
                            }
                            """))
                .andExpect(MockMvcResultMatchers.status().isCreated());
        Statement statement = statementRepository.findAll().get(0);
        UUID generatedStatementId = statement.getStatementId();

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                     "statementId": "%s",
                                     "requestedAmount": 500000,
                                     "totalAmount": 550000.00,
                                     "term": 12,
                                     "monthlyPayment": 49382.91,
                                     "rate": 14,
                                     "isInsuranceEnabled": true,
                                     "isSalaryClient": true
                                 }
                            """.formatted(generatedStatementId.toString()))
                )
                .andExpect(MockMvcResultMatchers.status().isAccepted());
        mockMvc.perform(MockMvcRequestBuilders.post("/deal/calculate/{statementId}", generatedStatementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "gender": "MALE",
                                    "maritalStatus": "SINGLE",
                                    "dependentAmount": 0,
                                    "passportIssueDate": "2015-03-23",
                                    "passportIssueBranch": "123-456",
                                    "employment": {
                                        "employmentStatus": "EMPLOYED",
                                        "employerINN": "123456789012",
                                        "salary": 100000,
                                        "position": "WORKER",
                                        "workExperienceTotal": 60,
                                        "workExperienceCurrent": 24
                                    },
                                    "accountNumber": "40817810099910004312"
                                }
                                """))
                .andExpect(MockMvcResultMatchers.status().isAccepted());
        Statement finalStatement = statementRepository.findById(generatedStatementId).orElseThrow();
        Client finalClient = finalStatement.getClient();
        assertEquals("123-456", finalClient.getPassport().getIssueBranch());
        assertEquals("40817810099910004312", finalClient.getAccountNumber());
        assertNotNull(finalClient.getEmployment(), "Данные о работе не сохранились!");

        assertEquals(ApplicationStatus.CC_APPROVED, finalStatement.getStatus(), "Статус заявки должен быть CC_APPROVED");
        assertTrue(finalStatement.getStatusHistory().size() >= 2, "История статусов не пополнилась");
        assertNotNull(finalStatement.getCredit(), "Кредит не был создан!");
        assertEquals(CreditStatus.CALCULATED, finalStatement.getCredit().getCreditStatus());
        assertEquals(0, BigDecimal.valueOf(500000).compareTo(finalStatement.getCredit().getAmount()));
    }
}
