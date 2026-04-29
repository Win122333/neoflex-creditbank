package neoflex.chulkov.service;

import neoflex.chulkov.dto.CreditDto;
import neoflex.chulkov.dto.CreditIssuedDto;
import neoflex.chulkov.dto.EmailMessage;
import neoflex.chulkov.dto.EmailSendDocumentsDto;
import neoflex.chulkov.dto.Message;
import neoflex.chulkov.dto.SesMessageDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.thymeleaf.context.Context;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DossierServiceTest {
    @Mock
    private MailSenderService mailSenderService;
    @Mock
    private Acknowledgment acknowledgment;
    @InjectMocks
    private DossierService dossierService;

    @Test
    void consumeCreateDocuments_ShouldSendEmailAndAcknowledge() {
        EmailMessage dto = new EmailMessage(
            "Test",
            "Test",
            "Test",
            "test@test.com",
            "b518d2b4-ab12-4c12-9373-34a340cc6578",
            LocalDate.of(2000, 12, 12)
        );
        dossierService.consumeCreateDocuments(dto, acknowledgment);
        verify(mailSenderService).send(any(Message.class), eq("mail/create-documents"), any(Context.class));

        verify(acknowledgment).acknowledge();
    }
    @Test
    void consumeFinishRegistration_ShouldSendEmailAndAcknowledge() {
        EmailMessage dto = new EmailMessage(
            "Test",
            "Test",
            "Test",
            "test@test.com",
            "b518d2b4-ab12-4c12-9373-34a340cc6578",
            LocalDate.of(2000, 12, 12)
        );
        dossierService.consumeFinishRegistration(dto, acknowledgment);
        verify(mailSenderService).send(any(Message.class), eq("mail/finish-registration"), any(Context.class));

        verify(acknowledgment).acknowledge();
    }
    @Test
    void consumeSendSes_ShouldSendEmailAndAcknowledge() {
        SesMessageDto dto = new SesMessageDto(
            "123123",
            "test@test.com",
            "b518d2b4-ab12-4c12-9373-34a340cc6578"
        );
        dossierService.consumeSendSes(dto, acknowledgment);
        verify(mailSenderService).send(any(Message.class), eq("mail/sign-ses-documents"), any(Context.class));

        verify(acknowledgment).acknowledge();
    }
    @Test
    void consumeCreditIssued_ShouldSendEmailAndAcknowledge() {
        CreditIssuedDto dto = new CreditIssuedDto(
            "test@test.com",
            "Test",
            "Test",
            "b518d2b4-ab12-4c12-9373-34a340cc6578"
        );
        dossierService.consumeCreditIssued(dto, acknowledgment);
        verify(mailSenderService).send(any(Message.class), eq("mail/credit-issued"), any(Context.class));

        verify(acknowledgment).acknowledge();
    }
    @Test
    void consumeSendDocuments_ShouldSendEmailAndAcknowledge() {
        EmailSendDocumentsDto dto = new EmailSendDocumentsDto(
            "Test",
            "Test",
            "Test",
            "test@test.com",
            "b518d2b4-ab12-4c12-9373-34a340cc6578",
            null
        );
        dossierService.consumeSendDocuments(dto, acknowledgment);
        verify(mailSenderService).send(any(Message.class), eq("mail/send-documents"), any(Context.class));

        verify(acknowledgment).acknowledge();
    }
}