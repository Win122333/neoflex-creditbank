package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.client.LoanOfferRestClient;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.dto.StatementStatusHistoryDto;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.dto.enums.ChangeType;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Passport;
import neoflex.chulkov.entity.Statement;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealService {
    private final LoanOfferRestClient loanOfferRestClient;
    private final ClientService clientService;
    private final StatementService statementService;


    public List<LoanOfferDto> statement(LoanStatementRequestDto dto) {
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

        return loanOfferRestClient.getAvailableOffers(dto)
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

    public void select(LoanOfferDto dto) {
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
}
