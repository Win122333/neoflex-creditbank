package neoflex.chulkov.entity;

import lombok.*;
import neoflex.chulkov.dto.enums.EmploymentPosition;
import neoflex.chulkov.dto.enums.EmploymentStatus;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employment {
    private EmploymentStatus employmentStatus;
    private String employerINN;
    private BigDecimal salary;
    private EmploymentPosition position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}
