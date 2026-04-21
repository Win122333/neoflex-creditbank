package neoflex.chulkov.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.service.DealService;
import neoflex.chulkov.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealApiDelegateImpl implements DealApiDelegate {
    private final DealService dealService;
    private final DocumentService documentService;

    @Override
    public ResponseEntity<Void> sendDocuments(String statementId) {
        log.info("called /deal/document/{statementId}/send with statementId = {}", statementId);
        return DealApiDelegate.super.sendDocuments(statementId);
    }

    @Override
    public ResponseEntity<Void> signDocuments(String statementId) {
        log.info("called /deal/document/{statementId}/sign with statementId = {}", statementId);
        return DealApiDelegate.super.signDocuments(statementId);
    }

    @Override
    public ResponseEntity<Void> codeDocuments(String statementId) {
        log.info("called /deal/document/{statementId}/code with statementId = {}", statementId);
        return DealApiDelegate.super.codeDocuments(statementId);
    }

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
