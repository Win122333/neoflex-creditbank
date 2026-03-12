package neoflex.chulkov.dto;

import neoflex.chulkov.dto.enums.EmploymentStatus;
import neoflex.chulkov.dto.enums.Position;

import java.math.BigDecimal;

public record EmploymentDto(
        EmploymentStatus employmentStatus,
        String employerINN,
        BigDecimal salary,
        Position position,
        Integer workExperienceTotal,
        Integer workExperienceCurrent
) {
}
