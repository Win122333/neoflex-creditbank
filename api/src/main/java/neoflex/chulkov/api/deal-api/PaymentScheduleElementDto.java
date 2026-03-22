package neoflex.chulkov.api.deal;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentScheduleElementDto(
        Integer number,
        LocalDate date,
        BigDecimal totalPayment,
        BigDecimal interestPayment,
        BigDecimal debtPayment,
        BigDecimal remainingDebt
) {
}
