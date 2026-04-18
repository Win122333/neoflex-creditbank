package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final KafkaProducerService kafkaProducerService;

    public void sendDocuments(String statementId) {
    }
}
