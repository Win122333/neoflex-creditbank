package neoflex.chulkov.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.EmailSendDocumentsDto;
import neoflex.chulkov.dto.SesMessageDto;
import neoflex.chulkov.dto.StatementStatusHistoryDto;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.dto.enums.ChangeType;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.InvalidStatementStatusException;
import neoflex.chulkov.mapper.CreditMapper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {
    private final StatementService statementService;
    private final KafkaProducerService kafkaProducerService;
    private final CreditMapper creditMapper;

    @Transactional
    public void sendDocuments(String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));
        if (!statement.getStatus().equals(ApplicationStatus.CC_APPROVED)) {
            throw new InvalidStatementStatusException("Заявка не находится в статусе CC_APPROVED");
        }

        statement.setStatus(ApplicationStatus.DOCUMENT_CREATED);
        statement.getStatusHistory().add(new StatementStatusHistoryDto(
                ApplicationStatus.DOCUMENT_CREATED,
                OffsetDateTime.now(),
                ChangeType.AUTOMATIC
        ));
        log.info("статус заявки изменен на DOCUMENT_CREATED");
        log.debug("собираем EmailCreateDocumentsDto и отправляем в кафку");
        Client client = statement.getClient();
        EmailSendDocumentsDto message = new EmailSendDocumentsDto()
                .creditDto(creditMapper.toCreditDto(statement.getCredit()))
                .email(client.getEmail())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .middleName(client.getMiddleName())
                .statementId(statementId);
        statementService.saveStatement(statement);
        log.info("creditDto {}", statement.getCredit());


        kafkaProducerService.sendDocuments(message);
        log.info("Отправлено сообщение в топик send-documents");
    }

    @Transactional
    public void signDocument(String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));
        if (!statement.getStatus().equals(ApplicationStatus.DOCUMENT_CREATED)) {
            throw new InvalidStatementStatusException("Заявка не находится в статусе DOCUMENT_CREATED");
        }

        Client client = statement.getClient();
        String sesCode = generateSesCode();
        statement.setSesCode(sesCode);
        statementService.saveStatement(statement);
        log.debug("сохранили в statement ses code");

        SesMessageDto message = new SesMessageDto()
                .ses(sesCode)
                .email(client.getEmail())
                .statementId(statementId);

        kafkaProducerService.sendSes(message);
        log.info("Отправлено сообщение в топик sign-document");
    }

    private String generateSesCode() {
        SecureRandom rnd = new SecureRandom();
        log.info("сгенерирован ses code");
        return String.valueOf(100_000 + rnd.nextInt(900_000));
    }
}
