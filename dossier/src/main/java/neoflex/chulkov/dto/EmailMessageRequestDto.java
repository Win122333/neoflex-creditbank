package neoflex.chulkov.dto;

import java.time.LocalDate;

public record EmailMessageRequestDto(
        String firstName,
        String lastName,
        String middleName,
        String email,
        String statementId,
        LocalDate birthday
) {
}
