package neoflex.chulkov.client;

import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public interface LoanOfferRestClient {
    List<LoanOfferDto> getAvailableOffers(LoanStatementRequestDto requestDto);
}
