package neoflex.chulkov.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import neoflex.chulkov.dto.enums.EmploymentStatus;
import neoflex.chulkov.dto.enums.Position;

import java.math.BigDecimal;
@Builder(toBuilder = true)
public record EmploymentDto(
        @NotNull
        EmploymentStatus employmentStatus,
        @NotBlank
        @Pattern(regexp = "^\\d{12}$", message = "ИНН некорректен")
        String employerINN,
        @NotNull(message = "зарплата не может быть пустой")
        @Min(value = 0, message = "зарплата не может быть < 0")
        BigDecimal salary,
        Position position,
        @NotNull(message = "Стаж не может быть пустым")
        @Min(value = 0, message = "Опыт работы не может быть отрицательным")
        Integer workExperienceTotal,
        @NotNull(message = "Стаж не может быть пустым")
        @Min(value = 0, message = "Опыт работы не может быть отрицательным")
        Integer workExperienceCurrent
) {
}
