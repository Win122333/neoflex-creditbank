package neoflex.chulkov.api;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.service.StatementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementApiDelegateImpl implements StatementApiDelegate {
    private final StatementService statementService;
    @Override
    public ResponseEntity<List<LoanOfferDto>> getAvailableCreditOffers(LoanStatementRequestDto loanStatementRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(statementService.getAvailableCreditOffers(loanStatementRequestDto));
    }

    @Override
    public ResponseEntity<Void> selectCreditOffer(LoanOfferDto loanOfferDto) {
        statementService.selectCreditOffer(loanOfferDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
