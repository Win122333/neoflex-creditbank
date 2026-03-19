package neoflex.chulkov.dto;

import neoflex.chulkov.dto.enums.Gender;
import neoflex.chulkov.dto.enums.MaritalStatus;

import java.time.LocalDate;

public record FinishRegistrationRequestDto(
        Gender gender,
        MaritalStatus maritalStatus,
        Integer dependentAmount,
        LocalDate passportIssueDate,
        String passportIssueBranch,
        EmploymentDto employment,
        String accountNumber
) {
}
