package neoflex.chulkov.repository;

import neoflex.chulkov.dto.enums.OutboxStatus;
import neoflex.chulkov.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
    List<Outbox> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
