package neoflex.chulkov.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import neoflex.chulkov.dto.EmailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @SneakyThrows
    public void send(EmailMessage message, String subject, String templateName, Context context) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom("credit-bank@neoflex.ru");
        helper.setTo(message.email());
        helper.setSubject(subject);

        String htmlContent = templateEngine.process(templateName, context);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }
}
