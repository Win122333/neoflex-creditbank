package neoflex.chulkov.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {
    @Bean
    public NewTopic finishRegistrationTopic(
            @Value("${deal.kafka.topic.finish-registration:finish-registration}") String topicName
    ) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(3)
                .build();
    }
    @Bean
    public NewTopic createDocumentsTopic(
            @Value("${deal.kafka.topic.create-documents:create-documents}") String topicName
    ) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(3)
                .build();
    }
    @Bean
    public NewTopic sendDocumentsTopic(
            @Value("${deal.kafka.topic.send-documents:send-documents}") String topicName
    ) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(3)
                .build();
    }
    @Bean
    public NewTopic sendSesTopic(
            @Value("${deal.kafka.topic.send-ses:send-ses}") String topicName
    ) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(3)
                .build();
    }
    @Bean
    public NewTopic creditIssuedTopic(
            @Value("${deal.kafka.topic.credit-issued:credit-issued}") String topicName
    ) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(3)
                .build();
    }
    @Bean
    public NewTopic statementDeniedTopic(
            @Value("${deal.kafka.topic.statement-denied:statement-denied}") String topicName
    ) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(3)
                .build();
    }
}
