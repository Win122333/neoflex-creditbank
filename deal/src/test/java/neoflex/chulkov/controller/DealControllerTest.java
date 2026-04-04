package neoflex.chulkov.controller;

import neoflex.chulkov.api.DealApiController;
import neoflex.chulkov.api.DealApiDelegate;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.exception.EmailAlreadyExistsException;
import neoflex.chulkov.exception.GlobalExceptionHandler;
import neoflex.chulkov.exception.InvalidStatementStatusException;
import neoflex.chulkov.exception.StatementNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(DealApiController.class)
@Import(GlobalExceptionHandler.class)
class DealControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private DealApiDelegate dealApiDelegate;

    static List<LoanOfferDto> listOfLoanOffers;

    private static final String VALID_REQUEST_JSON = """
            {
              "amount": 500000,
              "term": 12,
              "firstName": "Vlad",
              "lastName": "Simonyan",
              "middleName": "Igorevich",
              "email": "artem@example.com",
              "birthday": "1995-03-23",
              "passportSeries": "1234",
              "passportNumber": "567890"
            }
            """;

    @BeforeAll
    static void init() {
        listOfLoanOffers = List.of(
                new LoanOfferDto(
                        UUID.fromString("8d8be959-e8bb-4d06-a966-153eb13be940"),
                        BigDecimal.valueOf(500000),
                        BigDecimal.valueOf(550000.00),
                        12,
                        BigDecimal.valueOf(49382.91),
                        BigDecimal.valueOf(14),
                        true,
                        true
                ),
                new LoanOfferDto(
                        UUID.fromString("8d8be959-e8bb-4d06-a966-153eb13be940"),
                        BigDecimal.valueOf(500000),
                        BigDecimal.valueOf(550000.00),
                        12,
                        BigDecimal.valueOf(49382.91),
                        BigDecimal.valueOf(14),
                        true,
                        false
                ),
                new LoanOfferDto(
                        UUID.fromString("8d8be959-e8bb-4d06-a966-153eb13be940"),
                        BigDecimal.valueOf(500000),
                        BigDecimal.valueOf(550000.00),
                        12,
                        BigDecimal.valueOf(49382.91),
                        BigDecimal.valueOf(14),
                        false,
                        true
                ),
                new LoanOfferDto(
                        UUID.fromString("8d8be959-e8bb-4d06-a966-153eb13be940"),
                        BigDecimal.valueOf(500000),
                        BigDecimal.valueOf(550000.00),
                        12,
                        BigDecimal.valueOf(49382.91),
                        BigDecimal.valueOf(14),
                        false,
                        false
                )
        );
    }
    @Test
    void getStatement_ShouldReturnListOf4LoanOfferDto() throws Exception {

        Mockito.when(dealApiDelegate.statement(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(listOfLoanOffers));
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/deal/statement")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\n" +
                                        " \"amount\": 500000,\n" +
                                        " \"term\": 12,\n" +
                                        " \"firstName\": \"Vlad\",\n" +
                                        " \"lastName\": \"Simonyan\",\n" +
                                        " \"middleName\": \"Igorevich\",\n" +
                                        " \"email\": \"artem@example.com\",\n" +
                                        " \"birthday\": \"1995-03-23\",\n" +
                                        " \"passportSeries\": \"1234\",\n" +
                                        " \"passportNumber\": \"567890\"\n" +
                                        "}")
                )
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(4));
    }
    @Test
    void getStatement_ShouldReturnBadRequest() throws Exception {
        Mockito.when(dealApiDelegate.statement(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(listOfLoanOffers));
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/deal/statement")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\n" +
                                        " \"amount\": 500000,\n" +
                                        " \"term\": 12,\n" +
                                        " \"firstName\": \"Vlad\",\n" +
                                        " \"lastName\": \"S\",\n" +
                                        " \"middleName\": \"Igorevich\",\n" +
                                        " \"email\": \"artem@example.com\",\n" +
                                        " \"birthday\": \"1995-03-23\",\n" +
                                        " \"passportSeries\": \"1234\",\n" +
                                        " \"passportNumber\": \"567890\"\n" +
                                        "}")
                )
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("Должен вернуть 409 Conflict, если email уже существует")
    void statement_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        Mockito.when(dealApiDelegate.statement(any()))
                .thenThrow(new EmailAlreadyExistsException("artem@example.com"));
        mockMvc.perform(MockMvcRequestBuilders.post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Пользователь с таким email уже существует"))
                .andExpect(jsonPath("$.message").value("artem@example.com"));
    }

    @Test
    @DisplayName("Должен вернуть 404 Conflict, если заявка не найдена")
    void statement_ShouldReturnConflict_WhenStatementNotFound() throws Exception {
        Mockito.when(dealApiDelegate.statement(any()))
                .thenThrow(new StatementNotFoundException("Заявка с ID 123 не найдена"));

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Этой заявки не существует"))
                .andExpect(jsonPath("$.message").value("Заявка с ID 123 не найдена"));
    }

    @Test
    @DisplayName("Должен вернуть 409 Conflict при неверном статусе заявки")
    void statement_ShouldReturnConflict_WhenInvalidStatementStatus() throws Exception {
        Mockito.when(dealApiDelegate.statement(any()))
                .thenThrow(new InvalidStatementStatusException("Заявка уже одобрена"));

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Заявка в неверном статусе"))
                .andExpect(jsonPath("$.message").value("Заявка уже одобрена"));
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request и распарсить JSON от Калькулятора")
    void statement_ShouldReturnBadRequest_WhenCalculatorThrowsError() throws Exception {
        // Имитируем JSON-ошибку, которая прилетает от микросервиса Калькулятор
        String calculatorErrorJson = """
                {
                    "status": 400,
                    "error": "Возраст не может быть меньше 18 лет",
                    "message": "Ошибка валидации"
                }
                """;

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                null,
                calculatorErrorJson.getBytes(),
                null
        );

        Mockito.when(dealApiDelegate.statement(any())).thenThrow(exception);

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Возраст не может быть меньше 18 лет"))
                .andExpect(jsonPath("$.message").value("Ошибка валидации"));
    }
}