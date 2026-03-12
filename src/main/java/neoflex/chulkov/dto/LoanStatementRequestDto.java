package neoflex.chulkov.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanStatementRequestDto(
        BigDecimal amount,
        Integer term,
        String firstName,
        String lastName,
        String middleName,
        String email,
        LocalDate birthday,
        String passportSeries,
        String passportNumber
) {
}
