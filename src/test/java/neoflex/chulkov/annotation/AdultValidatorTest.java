package neoflex.chulkov.annotation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AdultValidatorTest {
    private AdultValidator validator;
    private ConstraintValidatorContext context;
    @BeforeEach
    void setUp() {
        validator = new AdultValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
    }
    @Test
    void shouldReturnFalse_WhenAgeIsSmallerThen18() {
        LocalDate nowMinus2Years = LocalDate.now().minusYears(2);
        assertFalse(validator.isValid(nowMinus2Years, context));
    }
    @Test
    void shouldReturnTrue_WhenAgeEquals18() {
        LocalDate nowMinus18Years = LocalDate.now().minusYears(18);
        assertTrue(validator.isValid(nowMinus18Years, context));
    }
    @Test
    void shouldReturnTrue_WhenAgeMoreThan18() {
        LocalDate nowMinus77Years = LocalDate.now().minusYears(77);
        assertTrue(validator.isValid(nowMinus77Years, context));
    }
    @Test
    void shouldReturnFalse_WhenAgeIsNull() {
        assertFalse(validator.isValid(null, context));
    }
}