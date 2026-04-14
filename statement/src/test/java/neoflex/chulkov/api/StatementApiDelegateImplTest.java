//package neoflex.chulkov.api;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import neoflex.chulkov.dto.LoanOfferDto;
//import neoflex.chulkov.dto.LoanStatementRequestDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.client.MockRestServiceServer;
//import org.springframework.test.web.client.match.MockRestRequestMatchers;
//import org.springframework.test.web.client.response.MockRestResponseCreators;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.client.RestClient;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@AutoConfigureMockRestServiceServer
//class StatementIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private RestClient restClient;
//
//    @Autowired
//    private MockRestServiceServer mockServer;
//
//    @BeforeEach
//    void setUp() {
//        mockServer.reset();
//    }
//
//    @Test
//    @DisplayName("Успешное получение предложений (POST /statement)")
//    void getAvailableCreditOffers_Success() throws Exception {
//        LoanStatementRequestDto requestDto = new LoanStatementRequestDto()
//                .amount(BigDecimal.valueOf(500000))
//                .term(18)
//                .firstName("Ivan")
//                .lastName("Ivanov")
//                .email("ivan@test.ru")
//                .birthday(LocalDate.of(1990, 1, 1))
//                .passportSeries("1234")
//                .passportNumber("123456");
//
//        LoanOfferDto mockOffer = new LoanOfferDto();
//        mockOffer.setStatementId(UUID.randomUUID());
//        mockOffer.setRate(BigDecimal.valueOf(15.0));
//
//        String mockDealResponse = objectMapper.writeValueAsString(List.of(mockOffer));
//
//        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:8082/deal/statement"))
//                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
//                .andRespond(MockRestResponseCreators.withSuccess(mockDealResponse, MediaType.APPLICATION_JSON));
//
//        mockMvc.perform(post("/statement")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].rate").value(15.0));
//
//        mockServer.verify();
//    }
//
//    @Test
//    @DisplayName("Ошибка валидации: возраст (POST /statement)")
//    void getAvailableCreditOffers_ValidationError_BadAge() throws Exception {
//        // Создаем заявку с возрастом меньше 18 лет (год рождения 2020)
//        LoanStatementRequestDto badRequestDto = new LoanStatementRequestDto()
//                .amount(BigDecimal.valueOf(500000))
//                .term(18)
//                .firstName("Иван")
//                .lastName("Иванов")
//                .email("ivan@test.ru")
//                .birthday(LocalDate.of(2020, 1, 1))
//                .passportSeries("1234")
//                .passportNumber("123456");
//
//
//        mockMvc.perform(post("/statement")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(badRequestDto)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value(400))
//                .andExpect(jsonPath("$.message").value("Ошибка валидации"));
//    }
//
//    @Test
//    @DisplayName("Успешный выбор предложения (POST /statement/offer)")
//    void selectCreditOffer_Success() throws Exception {
//        LoanOfferDto offerDto = new LoanOfferDto();
//        offerDto.setStatementId(UUID.randomUUID());
//
//        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:8082/deal/offer/select"))
//                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
//                .andRespond(MockRestResponseCreators.withSuccess());
//
//        mockMvc.perform(post("/statement/offer")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(offerDto)))
//                .andExpect(status().isAccepted());
//
//        mockServer.verify();
//    }
//}