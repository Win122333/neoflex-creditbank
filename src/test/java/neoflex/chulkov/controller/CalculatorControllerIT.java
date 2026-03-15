package neoflex.chulkov.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class CalculatorControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("При вызове вернет список из 4х LoanOfferDto отсортированный в порядке убывания ставки")
    void getAvailableOffers_Return4Offers() throws Exception {
        //given
        var request = MockMvcRequestBuilders
                .post("/calculator/offers")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"amount\": 500000,\n" +
                                "  \"term\": 12,\n" +
                                "  \"firstName\": \"Ivan\",\n" +
                                "  \"lastName\": \"Petrov\",\n" +
                                "  \"middleName\": \"Sergeevich\",\n" +
                                "  \"email\": \"ivan.petrov@example.com\",\n" +
                                "  \"birthday\": \"1990-05-15\",\n" +
                                "  \"passportSeries\": \"1234\",\n" +
                                "  \"passportNumber\": \"567890\"\n" +
                                "}");
        //when
        ResultActions resultActions = mockMvc.perform(request)
                .andExpectAll(status().isOk());

    }
    @Test
    @DisplayName("Возвращает CreditDto")
    void calculateCredit_ReturnCreditDto_WhereDataIsValid() throws Exception {
        //given
        var request = MockMvcRequestBuilders
                .post("/calculator/calc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "  \"amount\": 1000000,\n" +
                        "  \"term\": 24,\n" +
                        "  \"firstName\": \"Ivan\",\n" +
                        "  \"lastName\": \"Petrov\",\n" +
                        "  \"middleName\": \"Sergeevich\",\n" +
                        "  \"gender\": \"MALE\",\n" +
                        "  \"birthdate\": \"1990-05-15\",\n" +
                        "  \"passportSeries\": \"1234\",\n" +
                        "  \"passportNumber\": \"567890\",\n" +
                        "  \"passportIssueDate\": \"2010-06-20\",\n" +
                        "  \"passportIssueBranch\": \"УФМС России по г. Москва\",\n" +
                        "  \"maritalStatus\": \"MARRIED\",\n" +
                        "  \"dependentAmount\": 2,\n" +
                        "  \"employment\": {\n" +
                        "    \"employmentStatus\": \"EMPLOYED\",\n" +
                        "    \"employerINN\": \"770012345678\",\n" +
                        "    \"salary\": 150000,\n" +
                        "    \"position\": \"SPECIALIST\",\n" +
                        "    \"workExperienceTotal\": 60,\n" +
                        "    \"workExperienceCurrent\": 24\n" +
                        "  },\n" +
                        "  \"accountNumber\": \"40817810000000000001\",\n" +
                        "  \"isInsuranceEnabled\": true,\n" +
                        "  \"isSalaryClient\": true\n" +
                        "}");

        mockMvc.perform(request)
                .andExpectAll(status().isOk());
    }
    @Test
    @DisplayName("Данные не проходят скоринг")
    void calculateCredit_ThrowScoringException_WhereDataBadForScoring() throws Exception {
        //given
        var request = MockMvcRequestBuilders
                .post("/calculator/calc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "  \"amount\": 1000000,\n" +
                        "  \"term\": 24,\n" +
                        "  \"firstName\": \"Ivan\",\n" +
                        "  \"lastName\": \"Petrov\",\n" +
                        "  \"middleName\": \"Sergeevich\",\n" +
                        "  \"gender\": \"MALE\",\n" +
                        "  \"birthdate\": \"1990-05-15\",\n" +
                        "  \"passportSeries\": \"1234\",\n" +
                        "  \"passportNumber\": \"567890\",\n" +
                        "  \"passportIssueDate\": \"2010-06-20\",\n" +
                        "  \"passportIssueBranch\": \"УФМС России по г. Москва\",\n" +
                        "  \"maritalStatus\": \"MARRIED\",\n" +
                        "  \"dependentAmount\": 2,\n" +
                        "  \"employment\": {\n" +
                        "    \"employmentStatus\": \"UNEMPLOYED\",\n" +
                        "    \"employerINN\": \"770012345678\",\n" +
                        "    \"salary\": 150000,\n" +
                        "    \"position\": \"SPECIALIST\",\n" +
                        "    \"workExperienceTotal\": 60,\n" +
                        "    \"workExperienceCurrent\": 24\n" +
                        "  },\n" +
                        "  \"accountNumber\": \"40817810000000000001\",\n" +
                        "  \"isInsuranceEnabled\": true,\n" +
                        "  \"isSalaryClient\": true\n" +
                        "}");

        mockMvc.perform(request)
                .andExpect(status().is(422));
    }
    @Test
    @DisplayName("Данные не проходят валидацию")
    void calculateCredit_ThrowScoringException_WhereDataIsNotValidating() throws Exception {
        //given
        var request = MockMvcRequestBuilders
                .post("/calculator/calc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "  \"amount\": 1000000,\n" +
                        "  \"term\": 24,\n" +
                        "  \"firstName\": \"I\",\n" +
                        "  \"lastName\": \"Petrov\",\n" +
                        "  \"middleName\": \"Sergeevich\",\n" +
                        "  \"gender\": \"MALE\",\n" +
                        "  \"birthdate\": \"1990-05-15\",\n" +
                        "  \"passportSeries\": \"1234\",\n" +
                        "  \"passportNumber\": \"567890\",\n" +
                        "  \"passportIssueDate\": \"2010-06-20\",\n" +
                        "  \"passportIssueBranch\": \"УФМС России по г. Москва\",\n" +
                        "  \"maritalStatus\": \"MARRIED\",\n" +
                        "  \"dependentAmount\": 2,\n" +
                        "  \"employment\": {\n" +
                        "    \"employmentStatus\": \"EMPLOYED\",\n" +
                        "    \"employerINN\": \"770012345678\",\n" +
                        "    \"salary\": 150000,\n" +
                        "    \"position\": \"SPECIALIST\",\n" +
                        "    \"workExperienceTotal\": 60,\n" +
                        "    \"workExperienceCurrent\": 24\n" +
                        "  },\n" +
                        "  \"accountNumber\": \"40817810000000000001\",\n" +
                        "  \"isInsuranceEnabled\": true,\n" +
                        "  \"isSalaryClient\": true\n" +
                        "}");

        mockMvc.perform(request)
                .andExpect(status().is(400));
    }
}