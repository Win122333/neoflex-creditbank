package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.client.statement.StatementApi;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementService {
    private final StatementApi statementRestClient;

    public List<LoanOfferDto> getAvailableCreditOffers(LoanStatementRequestDto loanStatementRequestDto) {
        return statementRestClient.getAvailableCreditOffers(loanStatementRequestDto).getBody();
    }

    public void selectCreditOffer(LoanOfferDto loanOfferDto) {
        statementRestClient.selectCreditOffer(loanOfferDto);
    }
}
