package neoflex.chulkov.entity;

import neoflex.chulkov.dto.enums.EmploymentPosition;
import neoflex.chulkov.dto.enums.EmploymentStatus;

import java.math.BigDecimal;

public record Employment (
        EmploymentStatus status,
        String employerInn,
        BigDecimal salary,
        EmploymentPosition position,
        Integer workExperienceTotal,
        Integer workExperienceCurrent
) {
}
