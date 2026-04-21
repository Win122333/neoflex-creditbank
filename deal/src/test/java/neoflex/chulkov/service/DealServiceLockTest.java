package neoflex.chulkov.service;

import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.InvalidStatementStatusException;
import neoflex.chulkov.repository.ClientRepository;
import neoflex.chulkov.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class DealServiceLockTest {

    @Autowired
    private DealService dealService;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private ClientRepository clientRepository;

    private UUID statementId;

    @BeforeEach
    void setUp() {
        statementRepository.deleteAll();
        clientRepository.deleteAll();

        Client client = new Client();
        client.setFirstName("Test");
        client.setLastName("User");
        client.setEmail("test@example.com");
        client.setBirthday(LocalDate.of(1990, 1, 1));
        client = clientRepository.save(client);

        Statement statement = new Statement();
        statement.setClient(client);
        statement.setStatus(ApplicationStatus.PREAPPROVAL);
        statement = statementRepository.save(statement);

        statementId = statement.getStatementId();
    }

    @Test
    @DisplayName("Блокировка БД: параллельные вызовы selectOffer должны выполняться последовательно")
    void selectOffer_WithPessimisticLock_ShouldExecuteSequentially() throws InterruptedException {
        // given
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    LoanOfferDto offer = new LoanOfferDto();
                    offer.setStatementId(statementId);
                    offer.setRequestedAmount(BigDecimal.valueOf(500000));
                    offer.setTotalAmount(BigDecimal.valueOf(500000));
                    offer.setTerm(12);
                    offer.setMonthlyPayment(BigDecimal.valueOf(45000));
                    offer.setRate(BigDecimal.valueOf(15));
                    offer.setIsInsuranceEnabled(false);
                    offer.setIsSalaryClient(false);

                    try {
                        dealService.selectOffer(offer);
                        successCount.incrementAndGet();
                    } catch (InvalidStatementStatusException e) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertTrue(completed, "Все потоки должны завершиться в течение таймаута");
        assertEquals(1, successCount.get(), "Только один поток должен успешно обновить заявку");
        assertEquals(threadCount - 1, failureCount.get(), "Остальные потоки должны получить ошибку статуса");

        Statement finalStatement = statementRepository.findById(statementId).orElseThrow();
        assertEquals(ApplicationStatus.APPROVED, finalStatement.getStatus());
        assertEquals(1, finalStatement.getStatusHistory().size());
    }

    @Test
    @DisplayName("Блокировка БД: проверка времени ожидания при параллельных вызовах")
    void selectOffer_WithLock_ShouldShowWaitingTime() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch thread1Started = new CountDownLatch(1);
        CountDownLatch thread2Finished = new CountDownLatch(1);

        long[] thread1Duration = new long[1];
        long[] thread2Duration = new long[1];

        LoanOfferDto offer1 = createTestOffer(statementId);
        LoanOfferDto offer2 = createTestOffer(statementId);
        offer2.setRate(BigDecimal.valueOf(10));

        executor.submit(() -> {
            try {
                thread1Started.countDown();
                long start = System.currentTimeMillis();

                dealService.selectOffer(offer1);

                thread1Duration[0] = System.currentTimeMillis() - start;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread1Started.await();
        Thread.sleep(100);

        executor.submit(() -> {
            try {
                long start = System.currentTimeMillis();

                try {
                    dealService.selectOffer(offer2);
                } catch (InvalidStatementStatusException e) {
                }

                thread2Duration[0] = System.currentTimeMillis() - start;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                thread2Finished.countDown();
            }
        });

        // then
        boolean completed = thread2Finished.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Второй поток должен завершиться");
        assertTrue(thread2Duration[0] > thread1Duration[0],
                "Второй поток должен ждать дольше из-за блокировки. Thread1: " +
                        thread1Duration[0] + "ms, Thread2: " + thread2Duration[0] + "ms");
    }

    @Test
    @DisplayName("Блокировка БД: проверка что блокировка работает на уровне БД, а не приложения")
    void selectOffer_LockAtDatabaseLevel_ShouldWorkAcrossTransactions() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger firstSuccess = new AtomicInteger(0);
        AtomicInteger secondSuccess = new AtomicInteger(0);

        // when
        executor.submit(() -> {
            try {
                dealService.selectOffer(createTestOffer(statementId));
                firstSuccess.incrementAndGet();
            } catch (Exception e) {
                log.error("First transaction failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                Thread.sleep(50);
                dealService.selectOffer(createTestOffer(statementId));
                secondSuccess.incrementAndGet();
            } catch (InvalidStatementStatusException e) {
                log.error("Second transaction correctly failed: " + e.getMessage());
            } catch (Exception e) {
                log.error("Second transaction error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        // then
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Тест должен завершиться в течение таймаута");
        assertEquals(1, firstSuccess.get(), "Первая транзакция должна быть успешной");
        assertEquals(0, secondSuccess.get(), "Вторая транзакция должна получить ошибку статуса");

        Statement statement = statementRepository.findById(statementId).orElseThrow();
        assertEquals(ApplicationStatus.APPROVED, statement.getStatus());
        assertEquals(1, statement.getStatusHistory().size());
    }

    private LoanOfferDto createTestOffer(UUID statementId) {
        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(statementId);
        offer.setRequestedAmount(BigDecimal.valueOf(500000));
        offer.setTotalAmount(BigDecimal.valueOf(500000));
        offer.setTerm(12);
        offer.setMonthlyPayment(BigDecimal.valueOf(45000));
        offer.setRate(BigDecimal.valueOf(15));
        offer.setIsInsuranceEnabled(false);
        offer.setIsSalaryClient(false);
        return offer;
    }
}