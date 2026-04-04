package neoflex.chulkov.client;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.dto.CreditDto;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.dto.ScoringDataDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CalculatorRestClient {
    private static final ParameterizedTypeReference<List<LoanOfferDto>> OFFERS_TYPE_REFERENCES =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public List<LoanOfferDto> getAvailableOffers(LoanStatementRequestDto loanStatementRequestDto) {
        return restClient
                .post()
                .uri("/calculator/offers")
                .body(loanStatementRequestDto).contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OFFERS_TYPE_REFERENCES);
    }

    public CreditDto getCredit(ScoringDataDto scoringDataDto) {
        return restClient
                .post()
                .uri("/calculator/calc")
                .body(scoringDataDto).contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CreditDto.class);
    }
}
