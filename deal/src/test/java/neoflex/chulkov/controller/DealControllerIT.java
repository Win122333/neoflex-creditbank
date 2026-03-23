package neoflex.chulkov.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import jakarta.transaction.Transactional;
import neoflex.chulkov.repository.ClientRepository;
import neoflex.chulkov.repository.StatementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
public class DealControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ClientRepository clientRepository;
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
                                  "birthday": "1995-03-23",
                                  "passportSeries": "1234",
                                  "passportNumber": "567890"
                                }
                                """, true, true))
                        .willReturn(WireMock.ok("""
                                [
                                    {
                                        "statementId": null,
                                        "requestedAmount": 500000,
                                        "totalAmount": 500000,
                                        "term": 12,
                                        "monthlyPayment": 46317.25,
                                        "rate": 20,
                                        "isInsuranceEnabled": false,
                                        "isSalaryClient": false
                                    },
                                    {
                                        "statementId": null,
                                        "requestedAmount": 500000,
                                        "totalAmount": 500000,
                                        "term": 12,
                                        "monthlyPayment": 46078.29,
                                        "rate": 19,
                                        "isInsuranceEnabled": false,
                                        "isSalaryClient": true
                                    },
                                    {
                                        "statementId": null,
                                        "requestedAmount": 500000,
                                        "totalAmount": 550000.00,
                                        "term": 12,
                                        "monthlyPayment": 49642.07,
                                        "rate": 15,
                                        "isInsuranceEnabled": true,
                                        "isSalaryClient": false
                                    },
                                    {
                                        "statementId": null,
                                        "requestedAmount": 500000,
                                        "totalAmount": 550000.00,
                                        "term": 12,
                                        "monthlyPayment": 49382.91,
                                        "rate": 14,
                                        "isInsuranceEnabled": true,
                                        "isSalaryClient": true
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
                .andExpectAll(MockMvcResultMatchers.status().isCreated(),
                        MockMvcResultMatchers.jsonPath("$.length()").value(4));

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
}
