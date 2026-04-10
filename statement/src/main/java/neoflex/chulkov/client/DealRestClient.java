package neoflex.chulkov.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class DealRestClient {
    private static final ParameterizedTypeReference<List<LoanOfferDto>> OFFERS_TYPE_REFERENCES =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public List<LoanOfferDto> getAvailableOffers(LoanStatementRequestDto requestDto) {
        log.info("Отправка запроса в МС Deal (/deal/statement) для расчета предложений {}", requestDto);

        List<LoanOfferDto> offers = restClient
                .post()
                .uri("/deal/statement")
                .body(requestDto)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OFFERS_TYPE_REFERENCES);

        log.info("Получено {}", offers);
        return offers;
    }

    public void selectOffer(LoanOfferDto loanOfferDto) {
        log.info("Отправка выбранного предложения в МС Deal (/deal/offer/select) для заявки ID: {}",
                loanOfferDto.getStatementId());

        restClient
                .post()
                .uri("/deal/offer/select")
                .body(loanOfferDto)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();

        log.info("Предложение для заявки ID: {} успешно принято МС Deal", loanOfferDto.getStatementId());
    }
}
