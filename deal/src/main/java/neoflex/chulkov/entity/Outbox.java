package neoflex.chulkov.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import neoflex.chulkov.dto.enums.OutboxStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Outbox {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "statement_id")
    private String statementId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;
    @Column(name = "topic", nullable = false)
    private String topic;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OutboxStatus status;
    @Column(name = "created_at")
    private Timestamp createdAt;
}
