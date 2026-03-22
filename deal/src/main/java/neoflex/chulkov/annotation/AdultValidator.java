package neoflex.chulkov.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


import java.time.LocalDate;
import java.time.Period;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    @Override
    public boolean isValid (
            LocalDate birthday, ConstraintValidatorContext constraintValidatorContext
    ) {
        if (birthday == null) {
            return false;
        }
        int age = Period.between(birthday, LocalDate.now()).getYears();
        return age >= 18;
    }
}
