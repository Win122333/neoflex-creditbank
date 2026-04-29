package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.CreditIssuedDto;
import neoflex.chulkov.dto.EmailMessage;
import neoflex.chulkov.dto.EmailSendDocumentsDto;
import neoflex.chulkov.dto.SesMessageDto;
import neoflex.chulkov.util.KafkaTopics;
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
    private final KafkaTopics kafkaTopics;

    public void sendFinishRegistration(EmailMessage message) {
        log.info("отправлено в kafka {} topic", kafkaTopics.getFinishRegistrationTopic());
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(kafkaTopics.getFinishRegistrationTopic(), message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendCreateDocuments(EmailMessage message) {
        log.info("отправлено в kafka {} topic", kafkaTopics.getCreateDocumentsTopic());
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(kafkaTopics.getCreateDocumentsTopic(), message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendSes(SesMessageDto message) {
        log.info("отправлено в kafka {} topic", kafkaTopics.getSesTopic());
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(kafkaTopics.getSesTopic(), message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendStatementDenied(EmailMessage message) {
        log.info("отправлено в kafka {} topic", kafkaTopics.getStatementDeniedTopic());
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(kafkaTopics.getStatementDeniedTopic(), message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendCreditIssued(CreditIssuedDto message) {
        log.info("отправлено в kafka {} topic", kafkaTopics.getCreditIssuedTopic());
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(kafkaTopics.getCreditIssuedTopic(), message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendDocuments(EmailSendDocumentsDto message) {
        log.info("отправлено в kafka {} topic", kafkaTopics.getSendDocumentsTopic());
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(kafkaTopics.getSendDocumentsTopic(), message.getStatementId(), message);
        future.whenComplete(handleCallbacks(message.getStatementId()));
    }
    public void sendFromOutbox(String topic, Object message, String statementId) {
        log.info("отправлено в kafka {} topic", topic);
        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(topic, statementId, message);
        future.whenComplete(handleCallbacks(statementId));
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
