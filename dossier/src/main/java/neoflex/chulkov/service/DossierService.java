package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.EmailMessage;
import neoflex.chulkov.dto.enums.Theme;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class DossierService {
    private final MailSenderService mailSenderService;

    @KafkaListener(topics = "finish-registration")
    public void consumeFinishRegistration(EmailMessage message, Acknowledgment acknowledgment) {
        log.info("Получено сообщение из Kafka finish-registration,  {}", message);

        sendMail("mail/finish-registration", message, acknowledgment, Theme.FINISH_REGISTRATION);
    }
    @KafkaListener(topics = "create-documents")
    public void consumeCreateDocuments(EmailMessage message, Acknowledgment acknowledgment) {
        log.info("Получено сообщение из Kafka create-documents,  {}", message);

        sendMail("mail/create-documents", message, acknowledgment, Theme.CREATE_DOCUMENT);
    }
    @KafkaListener(topics = "send-documents")
    public void consumeSendDocuments(EmailMessage message, Acknowledgment acknowledgment) {
        log.info("Получено сообщение из Kafka send-documents: {}", message);

        sendMail("mail/send-documents", message, acknowledgment, Theme.SEND_DOCUMENTS);
    }
    @KafkaListener(topics = "create-ses")
    public void consumeSendSes(EmailMessage message, Acknowledgment acknowledgment) {
        log.info("Получено сообщение из Kafka create-ses: {}", message);

        sendMail("mail/sing-ses-documents", message, acknowledgment, Theme.SEND_SES);
    }
    @KafkaListener(topics = "credit-issued")
    public void consumeCreditIssued(EmailMessage message, Acknowledgment acknowledgment) {
        log.info("Получено сообщение из Kafka credit-issued: {}", message);

        sendMail("mail/credit-issued", message, acknowledgment, Theme.CREDIT_ISSUED);
    }
    @KafkaListener(topics = "statement-denied")
    public void consumeStatementDenied(EmailMessage message, Acknowledgment acknowledgment) {
        log.info("Получено сообщение из Kafka statement-denied: {}", message);

        sendMail("mail/statement-denied", message, acknowledgment, Theme.STATEMENT_DENIED);
    }
    private void sendMail(
            String htmlTemplate,
            EmailMessage message,
            Acknowledgment acknowledgment,
            Theme theme
    ) {
        Context context = new Context();
        context.setVariable("client",message);

        try {
            mailSenderService.send(
                    message,
                    theme.getTitle(),
                    htmlTemplate,
                    context
            );
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Ошибка при отправке письма для заявки {}: {}",
                    message.statementId(), e.getMessage());
            throw e;
        }
    }
}
