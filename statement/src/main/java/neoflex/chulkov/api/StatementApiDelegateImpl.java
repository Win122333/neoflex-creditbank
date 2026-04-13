package neoflex.chulkov.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.service.StatementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementApiDelegateImpl implements StatementApiDelegate {

    private final StatementService statementService;

    @Override
    public ResponseEntity<List<LoanOfferDto>> getAvailableCreditOffers(
            LoanStatementRequestDto loanStatementRequestDto
    ) {
        log.info("called /statement with request = {}", loanStatementRequestDto);
        List<LoanOfferDto> response = statementService.getAvailableOffers(loanStatementRequestDto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> selectCreditOffer(
            LoanOfferDto loanOfferDto
    ) {
        log.info("called /statement/offer with request = {}", loanOfferDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
