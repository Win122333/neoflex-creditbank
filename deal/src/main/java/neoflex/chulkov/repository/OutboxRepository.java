package neoflex.chulkov.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import neoflex.chulkov.dto.enums.OutboxStatus;
import neoflex.chulkov.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    List<Outbox> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
