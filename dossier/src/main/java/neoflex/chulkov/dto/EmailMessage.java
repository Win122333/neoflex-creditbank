package neoflex.chulkov.dto;

import java.time.LocalDate;

public record EmailMessage(
        String firstName,
        String lastName,
        String middleName,
        LocalDate birthday,
        String email,
        String statementId
) {
}
