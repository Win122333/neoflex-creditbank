package neoflex.chulkov.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.client.DealRestClient;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementApiDelegateImpl implements StatementApiDelegate {

    private final DealRestClient dealRestClient;

    @Override
    public ResponseEntity<List<LoanOfferDto>> statement(
            LoanStatementRequestDto loanStatementRequestDto
    ) {
        log.info("called /statement with request = {}", loanStatementRequestDto);
        List<LoanOfferDto> response = dealRestClient.getAvailableOffers(loanStatementRequestDto);
        log.info("Ответ: {}", response);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> select(
            LoanOfferDto loanOfferDto
    ) {
        return StatementApiDelegate.super.select(loanOfferDto);
    }
}
