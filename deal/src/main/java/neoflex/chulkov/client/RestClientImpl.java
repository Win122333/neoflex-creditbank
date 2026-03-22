package neoflex.chulkov.client;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
@RequiredArgsConstructor
public class RestClientImpl implements LoanOfferRestClient {

    private static final ParameterizedTypeReference<List<LoanOfferDto>> OFFERS_TYPE_REFERENCES =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    @Override
    public List<LoanOfferDto> getAvailableOffers(LoanStatementRequestDto requestDto) {
        return restClient
                .post()
                .uri("/calculator/offers")
                .body(requestDto).contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OFFERS_TYPE_REFERENCES);
    }
}
