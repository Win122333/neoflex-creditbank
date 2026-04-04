package neoflex.chulkov.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskingMessageConverter extends MessageConverter {
    private static final Pattern MASKING_PATTERN = Pattern.compile(
            "(email|passportSeries|passportNumber|firstName|lastName|middleName)=([^,\\)]+)"
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        Matcher matcher = MASKING_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.replaceAll("$1=***");
        }
        return message;
    }
}
