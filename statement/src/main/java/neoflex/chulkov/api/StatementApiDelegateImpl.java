package neoflex.chulkov.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.service.StatementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementApiDelegateImpl implements StatementApiDelegate {

    private final StatementService statementService;

    /**
     * Обрабатывает запрос на прескоринг и получение предложений по кредиту.
     *
     * <ul>
     *   <li>Получает {@link LoanStatementRequestDto} через API.
     *   <li>Выполняет прескоринг заявки на основе переданных данных.
     *   <li>Возвращает список из 4 предложений {@link LoanOfferDto}, упорядоченных от наименее
     *       выгодного к наиболее выгодному.
     * </ul>
     *
     * @param loanStatementRequestDto данные заявки на кредит
     * @return список кредитных предложений {@link LoanOfferDto}
     */
    @Override
    public ResponseEntity<List<LoanOfferDto>> getAvailableCreditOffers(
            LoanStatementRequestDto loanStatementRequestDto
    ) {
        log.info("called /statement with");
        List<LoanOfferDto> response = statementService.getAvailableOffers(loanStatementRequestDto);
        return ResponseEntity.ok(response);
    }
    /**
     * Обрабатывает выбор кредитного предложения пользователем.
     *
     * <ul>
     *   <li>Получает {@link LoanOfferDto} через API.
     *   <li>Отправляет POST-запрос на эндпоинт /deal/offer/select в микросервис "Deal", где заявка с
     *       обновлённым статусом и сам кредит сохраняются в бд.
     * </ul>
     *
     * @param loanOfferDto выбранное кредитное предложение
     */
    @Override
    public ResponseEntity<Void> selectCreditOffer(
            LoanOfferDto loanOfferDto
    ) {
        log.info("called /statement/offer with request = {}", loanOfferDto);
        statementService.selectCreditOffer(loanOfferDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
