package neoflex.chulkov.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.service.DealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class DealApiDelegateImpl implements DealApiDelegate {
    private final DealService dealService;

    @Override
    public ResponseEntity<Void> calculate(
            String statementId,
            FinishRegistrationRequestDto finishRegistrationRequestDto
    ) {
        log.info("called /calculate/{statementId} with dto = {}", finishRegistrationRequestDto);
        dealService.calculateCredit(finishRegistrationRequestDto, statementId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<Void> select(
            LoanOfferDto loanOfferDto
    ) {
        log.info("called /offer/select with dto = {}", loanOfferDto);
        dealService.selectOffer(loanOfferDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<List<LoanOfferDto>> statement(
            LoanStatementRequestDto loanStatementRequestDto
    ) {
        log.info("called /statement with dto = {}", loanStatementRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.createStatement(loanStatementRequestDto));
    }
}
