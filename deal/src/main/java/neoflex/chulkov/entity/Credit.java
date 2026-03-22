package neoflex.chulkov.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import neoflex.chulkov.dto.PaymentScheduleElementDto;
import neoflex.chulkov.dto.enums.CreditStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "credit")
public class Credit {
    @Id
    @Column(name = "credit_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID creditId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "term")
    private Integer term;

    @Column(name = "montly_payment")
    private BigDecimal monthlyPayment;

    @Column(name = "rate")
    private BigDecimal rate;

    @Column(name = "psk")
    private BigDecimal psk;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_schedule", columnDefinition = "jsonb")
    private PaymentScheduleElementDto paymentSchedule;

    @Column(name = "insurance_enabled")
    private Boolean isnuranceEnabled;

    @Column(name = "salary_client")
    private Boolean salaryClient;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_status")
    private CreditStatus creditStatus;
}
