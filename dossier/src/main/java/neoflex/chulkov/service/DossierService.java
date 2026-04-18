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

        Context context = new Context();
        context.setVariable("client",message);

        try {
            mailSenderService.send(
                    message,
                    Theme.FINISH_REGISTRATION.getTitle(),
                    "finish-registration",
                    context
            );
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Ошибка при отправке письма для заявки {}: {}",
                    message.statementId(), e.getMessage());
            throw e;
        }
    }
//    @KafkaListener(topics = "create-documents")
//    public void consumeCreateDocuments(EmailMessage message) {
//        log.info("Получено сообщение из Kafka create-documents: {}", message);
//
//        Context context = new Context();
//        context.setVariable("mail", message.statementId());
//
//        mailSenderService.send(
//                message.emailAddress(),
//                "Завершение оформления кредита",
//                "email/finish-registration",
//                context
//        );
//    }
//    @KafkaListener(topics = "send-documents")
//    public void consumeSendDocuments(EmailMessage message) {
//        log.info("Получено сообщение из Kafka send-documents: {}", message);
//
//        Context context = new Context();
//        context.setVariable("mail", message.statementId());
//
//        mailSenderService.send(
//                message.emailAddress(),
//                "Завершение оформления кредита",
//                "email/finish-registration",
//                context
//        );
//    }
//    @KafkaListener(topics = "create-ses")
//    public void consumeSendSes(EmailMessage message) {
//        log.info("Получено сообщение из Kafka create-ses: {}", message);
//
//        Context context = new Context();
//        context.setVariable("mail", message.statementId());
//
//        mailSenderService.send(
//                message.emailAddress(),
//                "Завершение оформления кредита",
//                "email/finish-registration",
//                context
//        );
//    }
//    @KafkaListener(topics = "credit-issued")
//    public void consumeCreditIssued(EmailMessage message) {
//        log.info("Получено сообщение из Kafka credit-issued: {}", message);
//
//        Context context = new Context();
//        context.setVariable("mail", message.statementId());
//
//        mailSenderService.send(
//                message.emailAddress(),
//                "Завершение оформления кредита",
//                "email/finish-registration",
//                context
//        );
//    }
//    @KafkaListener(topics = "statement-denied")
//    public void consumeStatementDenied(EmailMessage message) {
//        log.info("Получено сообщение из Kafka statement-denied: {}", message);
//
//        Context context = new Context();
//        context.setVariable("mail", message.statementId());
//
//        mailSenderService.send(
//                message.emailAddress(),
//                "Завершение оформления кредита",
//                "email/finish-registration",
//                context
//        );
//    }
}
