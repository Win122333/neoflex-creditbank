package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.client.CalculatorRestClient;
import neoflex.chulkov.dto.*;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.dto.enums.ChangeType;
import neoflex.chulkov.dto.enums.CreditStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Credit;
import neoflex.chulkov.entity.Passport;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.mapper.CreditMapper;
import neoflex.chulkov.mapper.ScoringDataMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        Client client = new Client()
                .setEmail(dto.email())
                .setFirstName(dto.firstName())
                .setLastName(dto.lastName())
                .setBirthDate(dto.birthday())
                .setMiddleName(dto.middleName())
                .setPassport(Passport.builder()
                        .series(dto.passportSeries())
                        .build());
        clientService.save(client);
        Statement statement = new Statement()
                .setClient(client)
                .setStatus(ApplicationStatus.PREAPPROVAL);

        Statement savedStatement = statementService.save(statement);

        return calculatorRestClient.getAvailableOffers(dto)
                .stream()
                .map(offer -> new LoanOfferDto(
                        savedStatement.getStatement_id(),
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
        statementService.save(statement);
    }

    public void calculateCredit(FinishRegistrationRequestDto dto, String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));

        ScoringDataDto scoringData = scoringDataMapper.toScoringDataDto(statement, dto);
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
            statementService.save(statement);
        }
        catch (RuntimeException e) {//TODO сделать красиво
            statement.setStatus(ApplicationStatus.CC_DENIED);
            statementService.save(statement);
        }
    }
}
