package neoflex.chulkov.service;

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
import neoflex.chulkov.mapper.ClientMapper;
import neoflex.chulkov.mapper.CreditMapper;
import neoflex.chulkov.mapper.ScoringDataMapper;
import org.springframework.stereotype.Service;

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

    public List<LoanOfferDto> createStatement(LoanStatementRequestDto dto) {
        Client client = clientService.createClient(dto);
        Statement statement = statementService.createStatement(client);

        log.debug("save statement and client");
        return calculatorRestClient.getAvailableOffers(dto)
                .stream()
                .map(offer -> new LoanOfferDto(
                        statement.getStatement_id(),
                        offer.requestedAmount(),
                        offer.totalAmount(),
                        offer.term(),
                        offer.monthlyPayment(),
                        offer.rate(),
                        offer.isInsuranceEnabled(),
                        offer.isSalaryClient()))
                .toList();
    }

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

    public void calculateCredit(FinishRegistrationRequestDto dto, String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));
        //TODO насыщать Client
        ScoringDataDto scoringData = scoringDataMapper.toScoringDataDto(statement, dto);
        log.debug("calculateCredit with scoringData = {}", scoringData);
        try{
            CreditDto creditDto = calculatorRestClient.getCredit(scoringData);
            Credit creditEntity = creditMapper.toCredit(creditDto);
            creditEntity.setCreditStatus(CreditStatus.CALCULATED);
            creditService.save(creditEntity);
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
        catch (RuntimeException e) {//TODO сделать красиво
            statement.setStatus(ApplicationStatus.CC_DENIED);
            statementService.saveStatement(statement);
        }
    }
}
