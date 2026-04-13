package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.client.DealRestClient;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementService {

    private final DealRestClient dealRestClient;

    public List<LoanOfferDto> getAvailableOffers(
            LoanStatementRequestDto loanStatementRequestDto
    ) {
        log.info("Отправлено выбранное предложение кредита");
        return dealRestClient.getAvailableOffers(loanStatementRequestDto);
    }

    public void selectCreditOffer(LoanOfferDto loanOfferDto) {
        log.info("Получен запрос на выбор кредитного предложения для заявки ID: {}",
                loanOfferDto.getStatementId());
        dealRestClient.selectOffer(loanOfferDto);
        log.info("Запрос на выбор предложения (Заявка ID: {}) успешно передан в МС Deal",
                loanOfferDto.getStatementId());
    }
}
