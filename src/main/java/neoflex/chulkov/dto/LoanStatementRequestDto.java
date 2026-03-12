package neoflex.chulkov.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import neoflex.chulkov.annotation.Adult;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanStatementRequestDto(
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
        @Pattern(regexp = "^[a-z0-9A-Z_!#$%&'*+/=?`{|}~^.-]+@[a-z0-9A-Z.-]+$", message = "Неверный формат email")
        String email,
        @Past(message = "Дата рождения должна быть в прошлом")
        LocalDate birthday,
        @NotBlank(message = "Серия паспорта должна быть заполнена")
        @Pattern(regexp = "^\\d{4}$", message = "Серия паспорта должна состоять из 4 цифр")
        String passportSeries,
        @Adult(message = "Возраст не может быть меньше 18 лет")
        @NotBlank(message = "Номер паспорта должна быть заполнен")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd")
        @Pattern(regexp = "^\\d{6}$", message = "Номер паспорта должна состоять из 6 цифр")
        String passportNumber
) {
}
