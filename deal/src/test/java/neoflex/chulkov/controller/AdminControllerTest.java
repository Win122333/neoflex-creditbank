package neoflex.chulkov.controller;

import neoflex.chulkov.api.AdminApiController;
import neoflex.chulkov.api.AdminApiDelegate;
import neoflex.chulkov.dto.StatementDto;
import neoflex.chulkov.exception.GlobalExceptionHandler;
import neoflex.chulkov.exception.StatementNotFoundException;
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

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(AdminApiController.class)
@Import(GlobalExceptionHandler.class)
public class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminApiDelegate adminApiDelegate;

    @Test
    @DisplayName("Получение списка заявок: должен вернуть 200 OK и массив заявок")
    void getAllStatements_ShouldReturnOk() throws Exception {
        StatementDto statement1 = new StatementDto().statementId(UUID.randomUUID());
        StatementDto statement2 = new StatementDto().statementId(UUID.randomUUID());
        Mockito.when(adminApiDelegate.getAllStatements())
            .thenReturn(ResponseEntity.ok(List.of(statement1, statement2)));

        mockMvc.perform(get("/deal/admin/statement")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].statementId").value(statement1.getStatementId().toString()))
            .andExpect(jsonPath("$[1].statementId").value(statement2.getStatementId().toString()));
    }

    @Test
    @DisplayName("Получение заявки по ID: должен вернуть 200 OK")
    void getStatementById_ShouldReturnOk() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementDto statementDto = new StatementDto().statementId(statementId);
        Mockito.when(adminApiDelegate.getStatementById(statementId.toString()))
            .thenReturn(ResponseEntity.ok(statementDto));

        mockMvc.perform(get("/deal/admin/statement/{statementId}", statementId.toString())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statementId").value(statementId.toString()));
    }

    @Test
    @DisplayName("Получение заявки по ID: должен вернуть 404 Not Found, если заявка не существует")
    void getStatementById_ShouldReturnNotFound() throws Exception {
        String statementId = UUID.randomUUID().toString();
        String errorMessage = "Заявка не найдена с UUID == " + statementId;
        Mockito.when(adminApiDelegate.getStatementById(statementId))
            .thenThrow(new StatementNotFoundException(errorMessage));

        mockMvc.perform(get("/deal/admin/statement/{statementId}", statementId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").value(errorMessage));
    }
}