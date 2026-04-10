package neoflex.chulkov.api;

import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class StatementApiDelegateImpl implements StatementApiDelegate {
    @Override
    public ResponseEntity<List<LoanOfferDto>> statement(
            LoanStatementRequestDto loanStatementRequestDto
    ) {
        log.info("called /statement with request = {}", loanStatementRequestDto);
        log.info("Ответ: {}", response);
        return response;
    }

    @Override
    public ResponseEntity<Void> select(
            LoanOfferDto loanOfferDto
    ) {
        return StatementApiDelegate.super.select(loanOfferDto);
    }
}
