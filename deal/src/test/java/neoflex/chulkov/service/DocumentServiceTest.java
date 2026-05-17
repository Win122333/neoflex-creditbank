package neoflex.chulkov.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import neoflex.chulkov.dto.CreditDto;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Credit;
import neoflex.chulkov.entity.Outbox;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.InvalidStatementStatusException;
import neoflex.chulkov.exception.WrongSesCodeException;
import neoflex.chulkov.mapper.CreditMapper;
import neoflex.chulkov.util.KafkaTopics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock
    private StatementService statementService;
    @Mock
    private CreditMapper creditMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private OutboxService outboxService;
    @Mock
    private KafkaTopics kafkaTopics;
    @Mock
    private SesCodeService sesCodeService;

    @InjectMocks
    private DocumentService documentService;

    @Captor
    private ArgumentCaptor<Outbox> outboxCaptor;

    private Statement testStatement;
    private final String STATEMENT_ID = UUID.randomUUID().toString();
    private final String JSON_PAYLOAD = "{\"test\":\"json\"}";

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setEmail("test@test.com");
        client.setFirstName("Ivan");
        client.setLastName("Ivanov");

        testStatement = new Statement();
        testStatement.setStatementId(UUID.fromString(STATEMENT_ID));
        testStatement.setClient(client);
        testStatement.setCredit(new Credit());
        testStatement.setStatusHistory(new ArrayList<>());
    }
    @Test
    void sendDocuments_ShouldProcessAndSaveToOutbox_WhenStatusIsValid() throws JsonProcessingException {
        // Arrange
        testStatement.setStatus(ApplicationStatus.CC_APPROVED);
        when(statementService.getStatementById(UUID.fromString(STATEMENT_ID))).thenReturn(testStatement);
        when(creditMapper.toCreditDto(any())).thenReturn(new CreditDto());
        when(objectMapper.writeValueAsString(any())).thenReturn(JSON_PAYLOAD);
        when(kafkaTopics.getSendDocumentsTopic()).thenReturn("send-documents-topic");

        // Act
        documentService.sendDocuments(STATEMENT_ID);

        // Assert
        assertEquals(ApplicationStatus.DOCUMENT_CREATED, testStatement.getStatus());
        assertEquals(1, testStatement.getStatusHistory().size());

        verify(statementService).saveStatement(testStatement);
        verify(outboxService).save(outboxCaptor.capture());

        Outbox savedOutbox = outboxCaptor.getValue();
        assertEquals(JSON_PAYLOAD, savedOutbox.getPayload());
        assertEquals("send-documents-topic", savedOutbox.getTopic());
    }

    @Test
    void sendDocuments_ShouldThrowException_WhenStatusIsInvalid() {
        testStatement.setStatus(ApplicationStatus.PREAPPROVAL);
        when(statementService.getStatementById(UUID.fromString(STATEMENT_ID))).thenReturn(testStatement);

        assertThrows(InvalidStatementStatusException.class,
            () -> documentService.sendDocuments(STATEMENT_ID));

        verify(statementService, never()).saveStatement(any());
        verify(outboxService, never()).save(any());
    }

    @Test
    void signDocument_ShouldGenerateSesAndSaveToOutbox_WhenStatusIsValid() throws JsonProcessingException {
        testStatement.setStatus(ApplicationStatus.DOCUMENT_CREATED);
        String expectedSesCode = "123456";

        when(statementService.getStatementById(UUID.fromString(STATEMENT_ID))).thenReturn(testStatement);
        when(sesCodeService.generateSesCode()).thenReturn(expectedSesCode);
        when(objectMapper.writeValueAsString(any())).thenReturn(JSON_PAYLOAD);
        when(kafkaTopics.getSesTopic()).thenReturn("ses-topic");

        documentService.signDocument(STATEMENT_ID);

        assertEquals(expectedSesCode, testStatement.getSesCode());
        verify(statementService).saveStatement(testStatement);

        verify(outboxService).save(outboxCaptor.capture());
        Outbox savedOutbox = outboxCaptor.getValue();
        assertEquals("ses-topic", savedOutbox.getTopic());
        assertEquals(JSON_PAYLOAD, savedOutbox.getPayload());
    }
    @Test
    void signDocument_ShouldThrowException_WhenStatusIsInvalid() {
        testStatement.setStatus(ApplicationStatus.CC_APPROVED);
        when(statementService.getStatementById(UUID.fromString(STATEMENT_ID))).thenReturn(testStatement);

        assertThrows(InvalidStatementStatusException.class,
            () -> documentService.signDocument(STATEMENT_ID));
    }
    @Test
    void codeDocument_ShouldUpdateStatusAndSaveToOutbox_WhenSesCodeIsCorrect() throws JsonProcessingException {
        String validSesCode = "123456";
        testStatement.setSesCode(validSesCode);

        when(statementService.getStatementById(UUID.fromString(STATEMENT_ID))).thenReturn(testStatement);
        when(objectMapper.writeValueAsString(any())).thenReturn(JSON_PAYLOAD);
        when(kafkaTopics.getCreditIssuedTopic()).thenReturn("credit-issued-topic");

        documentService.codeDocument(STATEMENT_ID, validSesCode);

        assertEquals(ApplicationStatus.CREDIT_ISSUED, testStatement.getStatus());
        assertEquals(2, testStatement.getStatusHistory().size());

        verify(statementService, times(2)).saveStatement(testStatement);

        verify(outboxService).save(outboxCaptor.capture());
        Outbox savedOutbox = outboxCaptor.getValue();
        assertEquals("credit-issued-topic", savedOutbox.getTopic());
        assertEquals(JSON_PAYLOAD, savedOutbox.getPayload());
    }

    @Test
    void codeDocument_ShouldThrowException_WhenSesCodeIsIncorrect() {
        testStatement.setSesCode("123456"); // Правильный код
        when(statementService.getStatementById(UUID.fromString(STATEMENT_ID))).thenReturn(testStatement);

        assertThrows(WrongSesCodeException.class,
            () -> documentService.codeDocument(STATEMENT_ID, "000000"));

        verify(statementService, never()).saveStatement(any());
        verify(outboxService, never()).save(any());
    }
}