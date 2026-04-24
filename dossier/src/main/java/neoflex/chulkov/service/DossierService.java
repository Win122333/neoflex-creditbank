package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.EmailMessage;
import neoflex.chulkov.dto.Message;
import neoflex.chulkov.dto.EmailSendDocumentsDto;
import neoflex.chulkov.dto.SesMessageDto;
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

    @KafkaListener(topics = "dossier.kafka.topic.finish-registration")
    public void consumeFinishRegistration(EmailMessage dto, Acknowledgment ack) {
        log.info("Получено сообщение из Kafka finish-registration, {}", dto);
        processAndSend(dto, dto.email(), dto.statementId(), "mail/finish-registration", "client", Theme.FINISH_REGISTRATION, ack);
    }

    @KafkaListener(topics = "${dossier.kafka.topic.create-documents}")
    public void consumeCreateDocuments(EmailMessage dto, Acknowledgment ack) {
        log.info("Получено сообщение из Kafka create-documents, {}", dto);
        processAndSend(dto, dto.email(), dto.statementId(), "mail/create-documents", "client", Theme.CREATE_DOCUMENT, ack);
    }

    @KafkaListener(topics = "${dossier.kafka.topic.send-documents}")
    public void consumeSendDocuments(EmailSendDocumentsDto dto, Acknowledgment ack) {
        log.info("Получено сообщение из Kafka send-documents: {}", dto);
        processAndSend(dto, dto.email(), dto.statementId(), "mail/send-documents", "message", Theme.SEND_DOCUMENTS, ack);
    }

    @KafkaListener(topics = "${dossier.kafka.topic.send-ses}")
    public void consumeSendSes(SesMessageDto dto, Acknowledgment ack) {
        log.info("Получено сообщение из Kafka create-ses: {}", dto);
        processAndSend(dto, dto.email(), dto.statementId(), "mail/sign-ses-documents", "client", Theme.SEND_SES, ack);
    }

    /**
     * Универсальный метод для отправки любого письма
     * @param payload Сам объект DTO (любого класса), который уйдет в Thymeleaf
     * @param email Почта клиента
     * @param statementId ID заявки для логов
     * @param template Путь к HTML-шаблону
     * @param contextVar Имя переменной внутри HTML-шаблона (например, "client" или "message")
     * @param theme Тема письма
     * @param ack Подтверждение для Kafka
     */
    private void processAndSend(
            Object payload,
            String email,
            String statementId,
            String template,
            String contextVar,
            Theme theme,
            Acknowledgment ack
    ) {
        Context context = new Context();
        context.setVariable(contextVar, payload);

        Message emailMessage = new Message(email, theme);

        try {
            mailSenderService.send(emailMessage, template, context);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Ошибка при отправке письма для заявки {}: {}", statementId, e.getMessage());
            throw e;
        }
    }
}