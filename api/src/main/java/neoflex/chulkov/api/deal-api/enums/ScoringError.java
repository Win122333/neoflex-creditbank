package neoflex.chulkov.api.deal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum ScoringError {
    UNEMPLOYED("REJECT: безработный"),
    LOW_SALARY("REJECT: 24 зарплаты меньше суммы кредита"),
    BAD_AGE("REJECT: возраст должен быть от 20 до 65 лет"),
    LOW_TOTAL_EXP("REJECT: общий стаж работы меньше 18 месяцев"),
    LOW_CURRENT_EXP("REJECT: текущий стаж работы меньше 3 месяцев");

    private final String message;
}
