package neoflex.chulkov.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.EmailMessage;
import neoflex.chulkov.dto.EmailSendDocumentsDto;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.InvalidStatementStatusException;
import neoflex.chulkov.mapper.CreditMapper;
import neoflex.chulkov.mapper.EmailMessageMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {
    private final ClientService clientService;
    private final StatementService statementService;
    private final KafkaProducerService kafkaProducerService;
    private final EmailMessageMapper emailMessageMapper;
    private final CreditService creditService;
    private final CreditMapper creditMapper;

    @Transactional
    public void sendDocuments(String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));
        if (!statement.getStatus().equals(ApplicationStatus.CC_APPROVED)) {
            throw new InvalidStatementStatusException("Заявка не находится в статусе CC_APPROVED");
        }

        statement.setStatus(ApplicationStatus.PREPARE_DOCUMENTS);
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

        log.info("статус заявки изменен на PREPARE_DOCUMENTS");

        kafkaProducerService.sendDocuments(message);
        log.info("Отправлено сообщение в топик send-documents");
    }

    public void signDocument(String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));
        Client client = statement.getClient();
        EmailMessage message = emailMessageMapper.createEmailMessageDto(client, statementId);

        kafkaProducerService.sendSes(message);
        log.info("Отправлено сообщение в топик send-ses");
    }

    public void verifySesCode(String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));
        Client client = statement.getClient();
        EmailMessage message = emailMessageMapper.createEmailMessageDto(client, statementId);

        kafkaProducerService.sendCreditIssued(message);
        log.info("Отправлено сообщение в топик credit-issued");
    }
}
