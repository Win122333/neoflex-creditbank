package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.EmailMessage;
import neoflex.chulkov.dto.EmailSendDocumentsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${deal.kafka.topic.finish-registration:finish-registration}")
    private String finishRegistrationTopic;
    @Value("${deal.kafka.topic.create-documents:create-documents}")
    private String createDocumentsTopic;
    @Value("${deal.kafka.topic.send-documents:send-documents}")
    private String sendDocumentsTopic;
    @Value("${deal.kafka.topic.send-ses:send-ses}")
    private String sesTopic;
    @Value("${deal.kafka.topic.credit-issued:credit-issued}")
    private String creditIssuedTopic;
    @Value("${deal.kafka.topic.statement-denied:statement-denied}")
    private String statementDeniedTopic;

    public void sendFinishRegistration(EmailMessage message) {
        log.info("отправлено в kafka " + finishRegistrationTopic + " topic");
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(finishRegistrationTopic, message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendCreateDocuments(EmailMessage message) {
        log.info("отправлено в kafka " + createDocumentsTopic + " topic");
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(createDocumentsTopic, message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendSes(EmailMessage message) {
        log.info("отправлено в kafka " + sesTopic + " topic");
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(sesTopic, message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendStatementDenied(EmailMessage message) {
        log.info("отправлено в kafka " + statementDeniedTopic + " topic");
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(statementDeniedTopic, message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendCreditIssued(EmailMessage message) {
        log.info("отправлено в kafka " + creditIssuedTopic + " topic");
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(creditIssuedTopic, message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendDocuments(EmailSendDocumentsDto message) {
        log.info("отправлено в kafka " + sendDocumentsTopic + " topic");
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(sendDocumentsTopic, message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    private BiConsumer<SendResult<String, Object>, Throwable> handleCallbacks(String id) {
        return (res, e) -> {
            if (e == null) {
                log.info("Успешно отправлено в topic == {}, partition == {}, offset == {}, statementId == {}",
                        res.getRecordMetadata().topic(),
                        res.getRecordMetadata().partition(),
                        res.getRecordMetadata().offset(),
                        id);
            }
            else {
                log.error("Ошибка при отправке в kafka. Заявка == {} ||| {}",
                        id, e.getMessage(), e);
            }
        };
    }
}
