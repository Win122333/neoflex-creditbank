package neoflex.chulkov.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.EmailMessage;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Statement;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {
    private final ClientService clientService;
    private final StatementService statementService;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public void sendDocuments(String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));
        statement.setStatus(ApplicationStatus.PREPARE_DOCUMENTS);
        statementService.saveStatement(statement);
        log.info("статус заявки изменен на PREPARE_DOCUMENTS");

        Client client = statement.getClient();
        kafkaProducerService.sendDocuments(
                new EmailMessage()
                        .statementId(statementId)
                        .firstName(client.getFirstName())
                        .lastName(client.getLastName())
                        .middleName(client.getMiddleName())
                        .email(client.getEmail())
                        .birthday(client.getBirthDate())
        );
        log.info("Отправлено сообщение в топик send-documents");
    }

    public void signDocument(String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));
        Client client = statement.getClient();
        kafkaProducerService.sendSes(
                new EmailMessage()
                        .statementId(statementId)
                        .firstName(client.getFirstName())
                        .lastName(client.getLastName())
                        .middleName(client.getMiddleName())
                        .email(client.getEmail())
                        .birthday(client.getBirthDate())
        );
        log.info("Отправлено сообщение в топик send-ses");
    }

    public void verifySesCode(String statementId) {
        Statement statement = statementService.getStatementById(UUID.fromString(statementId));
        Client client = statement.getClient();
        kafkaProducerService.sendCreditIssued(
                new EmailMessage()
                        .statementId(statementId)
                        .firstName(client.getFirstName())
                        .lastName(client.getLastName())
                        .middleName(client.getMiddleName())
                        .email(client.getEmail())
                        .birthday(client.getBirthDate()));
        log.info("Отправлено сообщение в топик credit-issued");
    }
}
