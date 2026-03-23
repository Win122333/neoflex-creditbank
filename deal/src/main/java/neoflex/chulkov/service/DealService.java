package neoflex.chulkov.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.client.CalculatorRestClient;
import neoflex.chulkov.dto.*;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.dto.enums.ChangeType;
import neoflex.chulkov.dto.enums.CreditStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Credit;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.ScoringException;
import neoflex.chulkov.mapper.ClientMapper;
import neoflex.chulkov.mapper.CreditMapper;
import neoflex.chulkov.mapper.ScoringDataMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService {
    private final CalculatorRestClient calculatorRestClient;
    private final ClientService clientService;
    private final StatementService statementService;
    private final CreditService creditService;
    private final ScoringDataMapper scoringDataMapper;
    private final CreditMapper creditMapper;
    private final ClientMapper clientMapper;

    @Transactional
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto dto) {
        Client client = clientService.createClient(dto);
        Statement statement = statementService.createStatement(client);

        log.debug("save statement and client");
        return calculatorRestClient.getAvailableOffers(dto)
                .stream()
                .map(offer -> new LoanOfferDto(
                        statement.getStatementId(),
                        offer.requestedAmount(),
                        offer.totalAmount(),
                        offer.term(),
                        offer.monthlyPayment(),
                        offer.rate(),
                        offer.isInsuranceEnabled(),
                        offer.isSalaryClient()))
                .toList();
    }

    @Transactional
    public void selectOffer(LoanOfferDto dto) {
        Statement statement = statementService.getStatementById(dto.statementId());
        statement
                .setStatus(ApplicationStatus.APPROVED)
                .setAppliedOffer(dto);
        statement.getStatusHistory().add(
                new StatementStatusHistoryDto(
                        ApplicationStatus.APPROVED,
                        LocalDateTime.now(),
                        ChangeType.AUTOMATIC
        ));
        statementService.saveStatement(statement);
    }

    @Transactional
    public void calculateCredit(FinishRegistrationRequestDto dto, String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));

        clientService.updateClient(statement.getClient(), dto);

        ScoringDataDto scoringData = scoringDataMapper.toScoringDataDto(statement, dto);
        log.debug("calculateCredit with scoringData = {}", scoringData);
        try{
            CreditDto creditDto = calculatorRestClient.getCredit(scoringData);
            Credit creditEntity = creditMapper.toCredit(creditDto);
            creditEntity.setCreditStatus(CreditStatus.CALCULATED);
            creditService.saveCredit(creditEntity);
            statement
                    .setStatus(ApplicationStatus.CC_APPROVED)
                    .setCredit(creditEntity);
            statement.getStatusHistory().add(
                    new StatementStatusHistoryDto(
                            ApplicationStatus.CC_APPROVED,
                            LocalDateTime.now(),
                            ChangeType.AUTOMATIC
                    )
            );
            statementService.saveStatement(statement);
        }
        catch (ScoringException e) {
            log.warn("Отказ по заявке {}: {}", statementId, e.getMessage());
            statement.setStatus(ApplicationStatus.CC_DENIED);
            statement.getStatusHistory().add(new StatementStatusHistoryDto(
                    ApplicationStatus.CC_DENIED,
                    LocalDateTime.now(),
                    ChangeType.AUTOMATIC
            ));
            statementService.saveStatement(statement);
        }
    }
}
