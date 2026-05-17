package neoflex.chulkov.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KafkaTopics {
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
}
