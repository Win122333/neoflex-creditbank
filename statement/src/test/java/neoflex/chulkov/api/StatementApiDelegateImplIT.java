package neoflex.chulkov.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import neoflex.chulkov.dto.ErrorResponseDto;
import neoflex.chulkov.dto.LoanOfferDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
@ActiveProfiles("test")
class StatementApiDelegateImplIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("При валидных данных возвращает список из 4 предложений")
    void getAvailableCreditOffers_Return4Offers_WhenDataIsValid() throws Exception {
        // given
        String requestBody = """
                {
                  "amount": 500000,
                  "term": 12,
                  "firstName": "Ivan",
                  "lastName": "Petrov",
                  "middleName": "Sergeevich",
                  "email": "ivan.petrov@example.com",
                  "birthday": "1990-05-15",
                  "passportSeries": "1234",
                  "passportNumber": "567890"
                }
                """;

        // Мокаем ответ от Deal MS
        String dealResponse = """
                [
                  {
                    "statementId": "%s",
                    "requestedAmount": 500000,
                    "totalAmount": 500000,
                    "term": 12,
                    "monthlyPayment": 46245.50,
                    "rate": 20,
                    "isInsuranceEnabled": false,
                    "isSalaryClient": false
                  },
                  {
                    "statementId": "%s",
                    "requestedAmount": 500000,
                    "totalAmount": 500000,
                    "term": 12,
                    "monthlyPayment": 45890.25,
                    "rate": 19,
                    "isInsuranceEnabled": false,
                    "isSalaryClient": true
                  },
                  {
                    "statementId": "%s",
                    "requestedAmount": 500000,
                    "totalAmount": 550000.00,
                    "term": 12,
                    "monthlyPayment": 49850.75,
                    "rate": 15,
                    "isInsuranceEnabled": true,
                    "isSalaryClient": false
                  },
                  {
                    "statementId": "%s",
                    "requestedAmount": 500000,
                    "totalAmount": 550000.00,
                    "term": 12,
                    "monthlyPayment": 49420.30,
                    "rate": 14,
                    "isInsuranceEnabled": true,
                    "isSalaryClient": true
                  }
                ]
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        WireMock.stubFor(post(urlEqualTo("/deal/statement"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(dealResponse)));

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].rate").value(20))
                .andExpect(jsonPath("$[3].rate").value(14))
                .andReturn();

        // then
        List<LoanOfferDto> offers = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, LoanOfferDto.class)
        );

        assertEquals(4, offers.size());
        assertTrue(offers.get(0).getRate().compareTo(offers.get(1).getRate()) >= 0);
        assertTrue(offers.get(2).getRate().compareTo(offers.get(3).getRate()) >= 0);
    }

    @Test
    @DisplayName("Данные не проходят прескоринг - сумма меньше 20000")
    void getAvailableCreditOffers_ThrowValidationException_WhenAmountLessThanMinimum() throws Exception {
        // given
        String requestBody = """
                {
                  "amount": 19000,
                  "term": 12,
                  "firstName": "Ivan",
                  "lastName": "Petrov",
                  "email": "ivan.petrov@example.com",
                  "birthday": "1990-05-15",
                  "passportSeries": "1234",
                  "passportNumber": "567890"
                }
                """;

        // when & then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponseDto error = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorResponseDto.class
        );

        assertEquals(400, error.getStatus());
        assertTrue(error.getError().contains("20000"));
    }

    @Test
    @DisplayName("Данные не проходят валидацию - имя короче 2 символов")
    void getAvailableCreditOffers_ThrowValidationException_WhenFirstNameTooShort() throws Exception {
        // given
        String requestBody = """
                {
                  "amount": 500000,
                  "term": 12,
                  "firstName": "I",
                  "lastName": "Petrov",
                  "email": "ivan.petrov@example.com",
                  "birthday": "1990-05-15",
                  "passportSeries": "1234",
                  "passportNumber": "567890"
                }
                """;

        // when & then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponseDto error = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorResponseDto.class
        );

        assertEquals(400, error.getStatus());
        assertTrue(error.getError().contains("^[a-zA-Z]{2,30}?$"));
    }

    @Test
    @DisplayName("Данные не проходят валидацию - некорректный email")
    void getAvailableCreditOffers_ThrowValidationException_WhenEmailIsInvalid() throws Exception {
        // given
        String requestBody = """
                {
                  "amount": 500000,
                  "term": 12,
                  "firstName": "Ivan",
                  "lastName": "Petrov",
                  "email": "invalid-email",
                  "birthday": "1990-05-15",
                  "passportSeries": "1234",
                  "passportNumber": "567890"
                }
                """;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Данные не проходят валидацию - серия паспорта не 4 цифры")
    void getAvailableCreditOffers_ThrowValidationException_WhenPassportSeriesInvalid() throws Exception {
        // given
        String requestBody = """
                {
                  "amount": 500000,
                  "term": 12,
                  "firstName": "Ivan",
                  "lastName": "Petrov",
                  "email": "ivan.petrov@example.com",
                  "birthday": "1990-05-15",
                  "passportSeries": "12345",
                  "passportNumber": "567890"
                }
                """;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Данные не проходят валидацию - номер паспорта не 6 цифр")
    void getAvailableCreditOffers_ThrowValidationException_WhenPassportNumberInvalid() throws Exception {
        // given
        String requestBody = """
                {
                  "amount": 500000,
                  "term": 12,
                  "firstName": "Ivan",
                  "lastName": "Petrov",
                  "email": "ivan.petrov@example.com",
                  "birthday": "1990-05-15",
                  "passportSeries": "1234",
                  "passportNumber": "5678909"
                }
                """;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deal MS возвращает 409 - пользователь уже существует")
    void getAvailableCreditOffers_HandleConflictFromDeal_WhenUserAlreadyExists() throws Exception {
        // given
        String requestBody = """
                {
                  "amount": 500000,
                  "term": 12,
                  "firstName": "Ivan",
                  "lastName": "Petrov",
                  "email": "existing@mail.ru",
                  "birthday": "1990-05-15",
                  "passportSeries": "1234",
                  "passportNumber": "567890"
                }
                """;

        String errorResponse = """
                {
                  "status": 409,
                  "error": "Пользователь с таким email уже существует",
                  "message": "Клиент с данным email existing@mail.ru уже существует"
                }
                """;

        WireMock.stubFor(post(urlEqualTo("/deal/statement"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.CONFLICT.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(errorResponse)));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Пользователь с таким email уже существует"));
    }

    @Test
    @DisplayName("Успешный выбор кредитного предложения")
    void selectCreditOffer_ReturnAccepted_WhenOfferIsValid() throws Exception {
        // given
        String requestBody = """
                {
                  "statementId": "%s",
                  "requestedAmount": 500000,
                  "totalAmount": 500000,
                  "term": 12,
                  "monthlyPayment": 46245.50,
                  "rate": 20,
                  "isInsuranceEnabled": false,
                  "isSalaryClient": false
                }
                """.formatted(UUID.randomUUID());

        WireMock.stubFor(post(urlEqualTo("/deal/offer/select"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Выбор предложения - Deal MS возвращает ошибку")
    void selectCreditOffer_ThrowException_WhenDealReturnsError() throws Exception {
        // given
        String requestBody = """
                {
                  "statementId": "%s",
                  "requestedAmount": 500000,
                  "totalAmount": 500000,
                  "term": 12,
                  "monthlyPayment": 46245.50,
                  "rate": 20,
                  "isInsuranceEnabled": false,
                  "isSalaryClient": false
                }
                """.formatted(UUID.randomUUID());

        String errorResponse = """
                {
                  "status": 404,
                  "error": "Заявка не найдена",
                  "message": "Statement with id %s not found"
                }
                """.formatted(UUID.randomUUID());

        WireMock.stubFor(post(urlEqualTo("/deal/offer/select"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(errorResponse)));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}