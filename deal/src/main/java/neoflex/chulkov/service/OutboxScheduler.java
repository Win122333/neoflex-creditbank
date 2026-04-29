package neoflex.chulkov.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.enums.OutboxStatus;
import neoflex.chulkov.entity.Outbox;
import neoflex.chulkov.repository.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxRepository outboxRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void processOutboxMessage() {
        List<Outbox> messages = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.WAIT);
        if (messages.isEmpty()) {
            return;
        }
        for (Outbox message : messages) {
            try{
                kafkaProducerService.sendFromOutbox(message.getTopic(), message.getPayload(), message.getStatementId());
                message.setStatus(OutboxStatus.SENT);
            }
            catch (Exception e) {
                log.error("Ошибка при отправке сообщения {} из Outbox: {}", message.getId(), e.getMessage());
            }
        }
        outboxRepository.saveAll(messages);
    }
}
