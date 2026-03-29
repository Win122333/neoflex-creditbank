package neoflex.chulkov.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.client.CalculatorRestClient;
import neoflex.chulkov.dto.*;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.dto.enums.ChangeType;
import neoflex.chulkov.dto.enums.CreditStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Credit;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.InvalidStatementStatusException;
import neoflex.chulkov.exception.ScoringException;
import neoflex.chulkov.mapper.CreditMapper;
import neoflex.chulkov.mapper.ScoringDataMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService {
    private final CalculatorRestClient calculatorRestClient;
    private final ClientService clientService;
    private final StatementService statementService;
    private final CreditService creditService;
    private final ScoringDataMapper scoringDataMapper;
    private final CreditMapper creditMapper;

    @Transactional
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto dto) {
        log.info("Инициация процесса создания заявки на кредит");
        log.debug("Входящие данные для создания заявки: {}", dto);

        Client client = clientService.createClient(dto);
        Statement statement = statementService.createStatement(client);

        log.debug("save statement and client");
        log.info("Клиент и заявка успешно созданы. ID заявки: {}", statement.getStatementId());

        log.info("Запрос доступных предложений из калькулятора для заявки: {}", statement.getStatementId());
        return calculatorRestClient.getAvailableOffers(dto)
                .stream()
                .map(offer -> new LoanOfferDto(
                        statement.getStatementId(),
                        offer.getRequestedAmount(),
                        offer.getTotalAmount(),
                        offer.getTerm(),
                        offer.getMonthlyPayment(),
                        offer.getRate(),
                        offer.getIsInsuranceEnabled(),
                        offer.getIsSalaryClient()))
                .toList();
    }

    @Transactional
    public void selectOffer(LoanOfferDto dto) {
        log.info("Выбор кредитного предложения для заявки ID: {}", dto.getStatementId());
        log.debug("Параметры выбранного предложения: {}", dto);
        Statement statement = statementService.getStatementById(dto.getStatementId());
        log.debug("Текущий статус заявки {}: {}", dto.getStatementId(), statement.getStatus());

        if (statement.getStatus() != ApplicationStatus.PREAPPROVAL)
            throw new InvalidStatementStatusException("Заяка не находится в статусе PREAPPROVAL");

        statement
                .setStatus(ApplicationStatus.APPROVED)
                .setAppliedOffer(dto);
        statement.getStatusHistory().add(
                new StatementStatusHistoryDto(
                        ApplicationStatus.APPROVED,
                        OffsetDateTime.now(),
                        ChangeType.AUTOMATIC
                ));
        statementService.saveStatement(statement);

        log.info("Предложение успешно применено. Статус заявки {} обновлен на {}", dto.getStatementId(), ApplicationStatus.APPROVED);
    }

    @Transactional
    public void calculateCredit(FinishRegistrationRequestDto dto, String statementId) {
        log.info("Начало завершения регистрации и расчета кредита для заявки ID: {}", statementId);

        Statement statement = statementService.getStatementById(UUID.fromString(statementId));

        if(statement.getStatus() != ApplicationStatus.APPROVED) {
            log.warn("Отказ в расчете: заявка {} находится в неверном статусе {}", statementId, statement.getStatus());
            throw new InvalidStatementStatusException("Заявка находится в неверном статусе");
        }

        clientService.updateClient(statement.getClient(), dto);
        log.debug("Данные клиента для заявки {} успешно обновлены", statementId);

        ScoringDataDto scoringData = scoringDataMapper.toScoringDataDto(statement, dto);
        log.debug("calculateCredit with scoringData = {}", scoringData);
        log.info("Отправка данных на скоринг в калькулятор для заявки {}", statementId);

        try{
            CreditDto creditDto = calculatorRestClient.getCredit(scoringData);
            log.debug("Получен успешный ответ от калькулятора для заявки {}: {}", statementId, creditDto);

            Credit creditEntity = creditMapper.toCredit(creditDto);
            creditEntity.setCreditStatus(CreditStatus.CALCULATED);
            creditService.saveCredit(creditEntity);
            log.debug("Кредит сохранен в БД со статусом CALCULATED");

            statement
                    .setStatus(ApplicationStatus.CC_APPROVED)
                    .setCredit(creditEntity);
            statement.getStatusHistory().add(
                    new StatementStatusHistoryDto(
                            ApplicationStatus.CC_APPROVED,
                            OffsetDateTime.now(),
                            ChangeType.AUTOMATIC
                    )
            );
            statementService.saveStatement(statement);
            log.info("Заявка {} успешно прошла скоринг", statementId);
        }
        catch (ScoringException e) {
            log.warn("Отказ по заявке {}: {}", statementId, e.getMessage());
            statement.setStatus(ApplicationStatus.CC_DENIED);
            statement.getStatusHistory().add(new StatementStatusHistoryDto(
                    ApplicationStatus.CC_DENIED,
                    OffsetDateTime.now(),
                    ChangeType.AUTOMATIC
            ));
            statementService.saveStatement(statement);
            log.info("Статус заявки {} изменен на CC_DENIED из-за отказа скоринга", statementId);
        }
    }
}