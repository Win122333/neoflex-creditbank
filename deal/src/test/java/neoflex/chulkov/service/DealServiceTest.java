package neoflex.chulkov.service;

import neoflex.chulkov.client.CalculatorRestClient;
import neoflex.chulkov.dto.*;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.dto.enums.CreditStatus;
import neoflex.chulkov.dto.enums.ScoringError;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Credit;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.ScoringException;
import neoflex.chulkov.mapper.CreditMapper;
import neoflex.chulkov.mapper.ScoringDataMapper;
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

    @InjectMocks
    private DealService dealService;

    @Test
    @DisplayName("Создание заявки: должен создать клиента, заявление и вернуть офферы с ID заявки")
    void createStatement_Success() {
        // Given
        LoanStatementRequestDto requestDto = new LoanStatementRequestDto(
                BigDecimal.valueOf(500000), 12, "Ivan", "Ivanov", null,
                "test@test.com", null, "1234", "567890");

        Client mockClient = new Client();
        UUID statementId = UUID.randomUUID();
        Statement mockStatement = new Statement().setStatementId(statementId);

        LoanOfferDto mockOffer = new LoanOfferDto(null, BigDecimal.valueOf(500000), BigDecimal.valueOf(500000), 12, BigDecimal.valueOf(50000), BigDecimal.valueOf(20), false, false);

        when(clientService.createClient(requestDto)).thenReturn(mockClient);
        when(statementService.createStatement(mockClient)).thenReturn(mockStatement);
        when(calculatorRestClient.getAvailableOffers(requestDto)).thenReturn(List.of(mockOffer));

        List<LoanOfferDto> result = dealService.createStatement(requestDto);

        assertEquals(1, result.size());
        assertEquals(statementId, result.get(0).statementId());
        verify(clientService).createClient(requestDto);
        verify(statementService).createStatement(mockClient);
    }

    @Test
    @DisplayName("Выбор оффера: должен обновить статус заявления на APPROVED")
    void selectOffer_Success() {
        UUID id = UUID.randomUUID();
        LoanOfferDto offerDto = new LoanOfferDto(id, null, null, null, null, null, null, null);
        Statement mockStatement = new Statement().setStatementId(id).setStatusHistory(new ArrayList<>());

        when(statementService.getStatementById(id)).thenReturn(mockStatement);

        dealService.selectOffer(offerDto);

        assertEquals(ApplicationStatus.APPROVED, mockStatement.getStatus());
        assertEquals(1, mockStatement.getStatusHistory().size());
        verify(statementService).saveStatement(mockStatement);
    }

    @Test
    @DisplayName("Полный расчет: успех — статус CC_APPROVED и сохранение кредита")
    void calculateCredit_Success() {
        UUID id = UUID.randomUUID();
        FinishRegistrationRequestDto requestDto = FinishRegistrationRequestDto.builder().build();
        Statement mockStatement = new Statement().setClient(new Client()).setStatusHistory(new ArrayList<>());
        ScoringDataDto scoringData = ScoringDataDto.builder().build();
        CreditDto creditDto = CreditDto.builder().build();
        Credit creditEntity = new Credit();

        when(statementService.getStatementById(id)).thenReturn(mockStatement);
        when(scoringDataMapper.toScoringDataDto(eq(mockStatement), any())).thenReturn(scoringData);
        when(calculatorRestClient.getCredit(scoringData)).thenReturn(creditDto);
        when(creditMapper.toCredit(creditDto)).thenReturn(creditEntity);

        dealService.calculateCredit(requestDto, id.toString());

        assertEquals(ApplicationStatus.CC_APPROVED, mockStatement.getStatus());
        assertEquals(CreditStatus.CALCULATED, creditEntity.getCreditStatus());
        verify(creditService).saveCredit(creditEntity);
        verify(statementService).saveStatement(mockStatement);
    }

    @Test
    @DisplayName("Полный расчет: отказ скоринга — статус CC_DENIED")
    void calculateCredit_ScoringDenied() {
        UUID id = UUID.randomUUID();
        FinishRegistrationRequestDto requestDto =  FinishRegistrationRequestDto.builder().build();
        Statement mockStatement = new Statement().setClient(new Client()).setStatusHistory(new ArrayList<>());

        when(statementService.getStatementById(id)).thenReturn(mockStatement);
        when(scoringDataMapper.toScoringDataDto(any(), any())).thenReturn(ScoringDataDto.builder().build());

        when(calculatorRestClient.getCredit(any())).thenThrow(new ScoringException(ScoringError.BAD_AGE));

        dealService.calculateCredit(requestDto, id.toString());

        assertEquals(ApplicationStatus.CC_DENIED, mockStatement.getStatus());
        verify(statementService).saveStatement(mockStatement);
        verify(creditService, never()).saveCredit(any());
    }
}