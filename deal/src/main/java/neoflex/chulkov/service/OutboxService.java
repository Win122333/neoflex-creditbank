package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.entity.Outbox;
import neoflex.chulkov.repository.OutboxRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;

    public void save(Outbox outbox) {
        log.info("Сообщение с id == {} сохранено в outbox для отправки в топик == {}",
            outbox.getId(), outbox.getTopic());
        outboxRepository.save(outbox);
    }
}
