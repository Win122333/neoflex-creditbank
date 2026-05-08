package neoflex.chulkov.controller;

import neoflex.chulkov.api.DealApiController;
import neoflex.chulkov.api.DocumentApiController;
import neoflex.chulkov.api.DocumentApiDelegate;
import neoflex.chulkov.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(DocumentApiController.class)
@Import(GlobalExceptionHandler.class)
public class DocumentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DocumentApiDelegate documentApiDelegate;

    @Test
    @DisplayName("Подписание документов: должен вернуть 200 OK")
    void signDocuments_ShouldReturnOk() throws Exception {
        String statementId = UUID.randomUUID().toString();

        Mockito.when(documentApiDelegate.signDocuments(statementId))
            .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/document/{statementId}/sign", statementId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка SES кода: должен вернуть 200 OK при валидном коде")
    void codeDocuments_ShouldReturnOk() throws Exception {
        String statementId = UUID.randomUUID().toString();
        String sesCodeJson = """
                {
                  "ses": "123456"
                }
                """;

        Mockito.when(documentApiDelegate.verifySesCode(any(String.class), any()))
            .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/document/{statementId}/code", statementId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(sesCodeJson))
            .andExpect(status().isOk());
    }
    @Test
    @DisplayName("Отправка документов: должен вернуть 200 OK")
    void sendDocuments_ShouldReturnOk() throws Exception {
        String statementId = UUID.randomUUID().toString();

        Mockito.when(documentApiDelegate.sendDocuments(statementId))
            .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(MockMvcRequestBuilders.post("/deal/document/{statementId}/send", statementId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
