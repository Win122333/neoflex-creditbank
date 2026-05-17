package neoflex.chulkov.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import neoflex.chulkov.client.CalculatorRestClient;
import neoflex.chulkov.dto.*;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.dto.enums.CreditStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Credit;
import neoflex.chulkov.entity.Outbox;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.ScoringException;
import neoflex.chulkov.mapper.CreditMapper;
import neoflex.chulkov.mapper.EmailMessageMapper;
import neoflex.chulkov.mapper.ScoringDataMapper;
import neoflex.chulkov.util.KafkaTopics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {
    @Mock private CalculatorRestClient calculatorRestClient;
    @Mock private ClientService clientService;
    @Mock private StatementService statementService;
    @Mock private CreditService creditService;
    @Mock private ScoringDataMapper scoringDataMapper;
    @Mock private CreditMapper creditMapper;
    @Mock private EmailMessageMapper emailMessageMapper;
    @Mock private ObjectMapper objectMapper;

    @Mock private KafkaTopics kafkaTopics;
    @Mock private OutboxService outboxService;

    @InjectMocks
    private DealService dealService;

    @Test
    @DisplayName("Создание заявки: должен создать клиента, заявление и вернуть офферы с ID заявки")
    void createStatement_Success() {
        // Given
        LoanStatementRequestDto requestDto = new LoanStatementRequestDto()
            .amount(BigDecimal.valueOf(50000))
            .term(12)
            .firstName("Vlad")
            .lastName("Simonyan")
            .middleName("Igorevich")
            .email("arte2m@example.com")
            .birthday(null)
            .passportNumber("1234")
            .passportSeries("567890");

        Client mockClient = new Client();
        UUID statementId = UUID.randomUUID();
        Statement mockStatement = new Statement().setStatementId(statementId);

        LoanOfferDto mockOffer = new LoanOfferDto(null, BigDecimal.valueOf(500000), BigDecimal.valueOf(500000), 12, BigDecimal.valueOf(50000), BigDecimal.valueOf(20), false, false);

        when(clientService.createClient(requestDto)).thenReturn(mockClient);
        when(statementService.createStatement(mockClient)).thenReturn(mockStatement);
        when(calculatorRestClient.getAvailableOffers(requestDto)).thenReturn(List.of(mockOffer));

        List<LoanOfferDto> result = dealService.createStatement(requestDto);

        assertEquals(1, result.size());
        assertEquals(statementId, result.get(0).getStatementId());
        verify(clientService).createClient(requestDto);
        verify(statementService).createStatement(mockClient);
    }

    @Test
    @DisplayName("Выбор оффера: должен обновить статус заявления на APPROVED")
    void selectOffer_Success() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        LoanOfferDto offerDto = new LoanOfferDto(id, null, null, null, null, null, null, null);
        Statement mockStatement = new Statement().setClient(new Client()).setStatementId(id).setStatusHistory(new ArrayList<>()).setStatus(ApplicationStatus.PREAPPROVAL);

        when(statementService.getStatementById(id)).thenReturn(mockStatement);

        when(emailMessageMapper.createEmailMessageDto(any(), anyString())).thenReturn(new EmailMessage());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(kafkaTopics.getFinishRegistrationTopic()).thenReturn("finish-registration");

        dealService.selectOffer(offerDto);

        assertEquals(ApplicationStatus.APPROVED, mockStatement.getStatus());
        assertEquals(1, mockStatement.getStatusHistory().size());
        verify(statementService).saveStatement(mockStatement);
        verify(outboxService).save(any(Outbox.class)); // Проверяем, что сохранилось в Outbox
    }

    @Test
    @DisplayName("Полный расчет: успех — статус CC_APPROVED и сохранение кредита")
    void calculateCredit_Success() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        Statement mockStatement = new Statement().setClient(new Client()).setStatusHistory(new ArrayList<>()).setStatus(ApplicationStatus.APPROVED);
        ScoringDataDto scoringData = new ScoringDataDto();
        CreditDto creditDto = new CreditDto();
        Credit creditEntity = new Credit();

        when(statementService.getStatementById(id)).thenReturn(mockStatement);
        when(scoringDataMapper.toScoringDataDto(eq(mockStatement), any())).thenReturn(scoringData);
        when(calculatorRestClient.getCredit(scoringData)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto)).thenReturn(creditEntity);
        when(emailMessageMapper.createEmailMessageDto(any(Client.class), anyString())).thenReturn(new EmailMessage());

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(kafkaTopics.getCreateDocumentsTopic()).thenReturn("create-documents");

        dealService.calculateCredit(requestDto, id.toString());

        assertEquals(ApplicationStatus.CC_APPROVED, mockStatement.getStatus());
        assertEquals(CreditStatus.CALCULATED, creditEntity.getCreditStatus());
        verify(creditService).saveCredit(creditEntity);
        verify(statementService).saveStatement(mockStatement);
        verify(outboxService).save(any(Outbox.class)); // Проверяем, что сохранилось в Outbox
    }

    @Test
    @DisplayName("Полный расчет: отказ скоринга — статус CC_DENIED")
    void calculateCredit_ScoringDenied() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        FinishRegistrationRequestDto requestDto =  new FinishRegistrationRequestDto();
        Statement mockStatement = new Statement().setClient(new Client()).setStatusHistory(new ArrayList<>()).setStatus(ApplicationStatus.APPROVED);

        when(statementService.getStatementById(id)).thenReturn(mockStatement);
        when(scoringDataMapper.toScoringDataDto(any(), any())).thenReturn(new ScoringDataDto());
        when(emailMessageMapper.createEmailMessageDto(any(Client.class), anyString())).thenReturn(new EmailMessage());

        when(calculatorRestClient.getCredit(any())).thenThrow(new ScoringException("Отказ скоринга"));

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(kafkaTopics.getStatementDeniedTopic()).thenReturn("statement-denied");

        assertThrows(ScoringException.class, () -> dealService.calculateCredit(requestDto, id.toString()));

        assertEquals(ApplicationStatus.CC_DENIED, mockStatement.getStatus());
        verify(statementService).saveStatement(mockStatement);
        verify(creditService, never()).saveCredit(any());
        verify(outboxService).save(any(Outbox.class));
    }
}