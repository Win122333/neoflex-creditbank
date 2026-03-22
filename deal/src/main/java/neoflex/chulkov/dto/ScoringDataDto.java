package neoflex.chulkov.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import neoflex.chulkov.annotation.Adult;
import neoflex.chulkov.dto.enums.Gender;
import neoflex.chulkov.dto.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
@Builder(toBuilder = true)
public record ScoringDataDto (
        @NotNull(message = "Сумма кредита не может быть пустой")
        @Min(value = 20000, message = "Сумма кредита не может быть меньше 20000")
        BigDecimal amount,
        @NotNull
        @Min(value = 6, message = "Срок кредита не может быть меньше 6 месяцев")
        Integer term,
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z]{2,30}?$", message = "Имя должно состоять из 2 - 30 латинских символов")
        String firstName,
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z]{2,30}?$", message = "Фамилия должна состоять из 2 - 30 латинских символов")
        String lastName,
        @Pattern(regexp = "^[a-zA-Z]{2,30}?$", message = "Отчество должно состоять из 2 - 30 латинских символов")
        String middleName,
        @NotNull(message = "Пол не может быть пустым")
        Gender gender,
        @Adult
        @Past(message = "Дата рождения должна быть в прошлом")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate birthdate,
        @NotBlank(message = "Серия паспорта должна быть заполнена")
        @Pattern(regexp = "^\\d{4}$", message = "Серия паспорта должна состоять из 4 цифр")
        String passportSeries,
        @NotBlank(message = "Номер паспорта должна быть заполнен")
        @Pattern(regexp = "^\\d{6}$", message = "Номер паспорта должна состоять из 6 цифр")
        String passportNumber,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        @PastOrPresent(message = "Паспорт не может быть выдан в будущем")
        LocalDate passportIssueDate,
        @NotBlank(message = "Отделение выдачи паспорта не может быть пустым")
        String passportIssueBranch,
        @NotNull(message = "Семейное положение не может быть пустым")
        MaritalStatus maritalStatus,
        @NotNull
        @Min(value = 0)
        Integer dependentAmount,
        @Valid
        @NotNull(message = "Информация о работе не может быть пустой")
        EmploymentDto employment,
        @NotBlank
        String accountNumber,
        @NotNull
        Boolean isInsuranceEnabled,
        @NotNull
        Boolean isSalaryClient
) {
}
