package neoflex.chulkov.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatementRestClient {
    private final RestClient restClient;
    private static final ParameterizedTypeReference<List<LoanOfferDto>> OFFERS_TYPE_REFERENCES =
        new ParameterizedTypeReference<>() {
        };

    public List<LoanOfferDto> getAvailableCreditOffers(LoanStatementRequestDto loanStatementRequestDto) {
        log.info("sending request to statement service to get offers with LoanStatementRequestDto {}"
            , loanStatementRequestDto);
        return restClient.post()
            .uri("/statement")
            .body(loanStatementRequestDto).contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(OFFERS_TYPE_REFERENCES);
    }

    public void selectCreditOffer(LoanOfferDto loanOfferDto) {
        log.info("sending request to statement service to select offer with LoanOfferDto {}"
            , loanOfferDto);
        restClient.post()
            .uri("/statement/offer")
            .body(loanOfferDto).contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .toBodilessEntity();
    }
}
