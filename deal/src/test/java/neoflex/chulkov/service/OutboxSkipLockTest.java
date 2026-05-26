package neoflex.chulkov.service;

import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.enums.OutboxStatus;
import neoflex.chulkov.entity.Outbox;
import neoflex.chulkov.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class OutboxSkipLockTest {

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @MockitoBean
    private KafkaAdmin kafkaAdmin;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        List<Outbox> messages = new ArrayList<>();
        Instant now = Instant.now();

        for (int i = 0; i < 100; i++) {
            Outbox outbox = new Outbox();
            outbox.setStatus(OutboxStatus.WAIT);
            outbox.setTopic("test-topic");
            outbox.setPayload(String.format("{\"testData\": \"payload-%d\"}", i));

            outbox.setStatementId(UUID.randomUUID().toString());

            Instant pastTime = now.minus(100 - i, ChronoUnit.MINUTES);
            outbox.setCreatedAt(Timestamp.from(pastTime));

            messages.add(outbox);
        }
        outboxRepository.saveAll(messages);
    }

    @Test
    @DisplayName("SKIP LOCKED: два параллельных потока должны считать разные пакеты данных по 50 записей")
    void findTop50_WithSkipLocked_ShouldReturnDisjointSets() throws Exception {
        // given
        CountDownLatch firstThreadLocked = new CountDownLatch(1);
        CountDownLatch secondThreadDone = new CountDownLatch(1);

        // when
        CompletableFuture<List<Outbox>> future1 = CompletableFuture.supplyAsync(() ->
            transactionTemplate.execute(status -> {
                log.info("Поток 1: запрашиваю первые 50 записей...");
                List<Outbox> batch1 = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.WAIT);

                firstThreadLocked.countDown();

                try {
                    // Искусственно держим транзакцию открытой, пока Поток 2 не отработает
                    secondThreadDone.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("Поток 1: завершаю транзакцию");
                return batch1;
            })
        );

        CompletableFuture<List<Outbox>> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                firstThreadLocked.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return transactionTemplate.execute(status -> {
                log.info("Поток 2: запрашиваю следующие 50 записей...");
                List<Outbox> batch2 = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.WAIT);
                log.info("Поток 2: получил {} записей", batch2.size());

                secondThreadDone.countDown();
                return batch2;
            });
        });

        // then
        List<Outbox> batch1 = future1.get(15, TimeUnit.SECONDS);
        List<Outbox> batch2 = future2.get(15, TimeUnit.SECONDS);

        // Проверяем, что каждый поток получил ровно по 50 записей
        assertEquals(50, batch1.size(), "Первый поток должен вытащить 50 записей");
        assertEquals(50, batch2.size(), "Второй поток должен вытащить оставшиеся 50 записей (SKIP LOCKED сработал)");

        // Собираем ID записей из обеих пачек
        Set<UUID> ids1 = batch1.stream().map(Outbox::getId).collect(Collectors.toSet());
        Set<UUID> ids2 = batch2.stream().map(Outbox::getId).collect(Collectors.toSet());

        assertTrue(Collections.disjoint(ids1, ids2), "Потоки вытащили пересекающиеся записи! SKIP LOCKED не работает.");
    }
}