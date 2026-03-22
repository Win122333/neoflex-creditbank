package neoflex.chulkov.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import neoflex.chulkov.dto.StatementStatusHistoryDto;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "statement")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class Statement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID statement_id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "sign_date")
    private LocalDateTime signDate;

    @Column(name = "ses_code")
    private String sesCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "status_history", columnDefinition = "jsonb")
    private StatementStatusHistoryDto statusHistory;

    @OneToOne
    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    private Client client;

    @OneToOne
    @JoinColumn(name = "credit_id", referencedColumnName = "credit_id")
    private Credit credit;

    //TODO applied offer


}
