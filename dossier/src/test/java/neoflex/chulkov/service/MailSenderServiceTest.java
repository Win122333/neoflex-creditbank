package neoflex.chulkov.service;

import jakarta.mail.Address;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import neoflex.chulkov.dto.Message;
import neoflex.chulkov.dto.enums.Theme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private MailSenderService mailSenderService;

    @Captor
    private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    @Test
    void send_ShouldProcessTemplateAndSendMessage() throws Exception {
        String toEmail = "client@example.com";
        Theme theme = Theme.CREATE_DOCUMENT;
        Message message = new Message(toEmail, theme);

        String templateName = "mail/test-template";
        Context context = new Context();
        String expectedHtmlBody = "<h1>Привет, это тест!</h1>";
        MimeMessage realMimeMessage = new MimeMessage((Session) null);

        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        when(templateEngine.process(eq(templateName), eq(context))).thenReturn(expectedHtmlBody);

        mailSenderService.send(message, templateName, context);

        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(templateName, context);

        verify(mailSender).send(mimeMessageCaptor.capture());
        MimeMessage capturedMessage = mimeMessageCaptor.getValue();

        Address[] from = capturedMessage.getFrom();
        assertEquals(1, from.length);
        assertEquals("credit-bank@neoflex.ru", ((InternetAddress) from[0]).getAddress());

        Address[] recipients = capturedMessage.getAllRecipients();
        assertEquals(1, recipients.length);
        assertEquals(toEmail, ((InternetAddress) recipients[0]).getAddress());

        assertEquals(theme.getTitle(), capturedMessage.getSubject());
    }
}