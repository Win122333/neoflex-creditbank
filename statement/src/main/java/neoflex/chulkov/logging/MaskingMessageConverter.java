package neoflex.chulkov.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskingMessageConverter extends MessageConverter {

    private static final Pattern MASK_PATTERN = Pattern.compile(
            "(\"?\\b(email|passportSeries|passportNumber|firstName|lastName|middleName|birthday)\"?[=:]\\s*\"?)([^,\"\\s)]+)(\"?)"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}"
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || message.isEmpty()) return message;

        message = maskEmails(message);
        return maskSensitiveFields(message);
    }

    private String maskSensitiveFields(String message) {
        Matcher matcher = MASK_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String prefix = matcher.group(1);
            String field = matcher.group(2);
            String value = matcher.group(3);
            String suffix = matcher.group(4);

            String masked = switch (field) {
                case "email" -> maskEmail(value);
                case "firstName", "lastName", "middleName" -> maskName(value);
                default -> "***";
            };

            matcher.appendReplacement(sb, prefix + masked + suffix);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String maskEmails(String message) {
        Matcher matcher = EMAIL_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(sb, maskEmail(matcher.group()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String maskEmail(String email) {
        return "***@***.***";
    }

    private String maskName(String name) {
        return "***";
    }
}