package neoflex.chulkov.controller;

import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.service.DealService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(DealController.class)
class DealControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    DealService dealService;

    static List<LoanOfferDto> listOfLoanOffers;

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
        Mockito.when(dealService.createStatement(any()))
                .thenReturn(listOfLoanOffers);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"amount\": 500000,\n" +
                                "  \"term\": 12,\n" +
                                "  \"firstName\": \"Vlad\",\n" +
                                "  \"lastName\": \"Simonyan\",\n" +
                                "  \"middleName\": \"Igorevich\",\n" +
                                "  \"email\": \"artem@example.com\",\n" +
                                "  \"birthday\": \"1995-03-23\",\n" +
                                "  \"passportSeries\": \"1234\",\n" +
                                "  \"passportNumber\": \"567890\"\n" +
                                "}")
        )
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(4));
    }
}