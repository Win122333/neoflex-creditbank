package neoflex.chulkov.service;

import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.config.CalculatorProperties;
import neoflex.chulkov.dto.*;
import neoflex.chulkov.dto.enums.*;
import neoflex.chulkov.exception.ScoringException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class CalculatorServiceTest {
    private CalculatorService service;
    @BeforeEach
    void setUp() {
        CalculatorProperties.Salary salaryProps = new CalculatorProperties.Salary(
                BigDecimal.valueOf(1)
        );
        CalculatorProperties.Insurance insuranceProps = new CalculatorProperties.Insurance(
                new BigDecimal("0.10"),
                BigDecimal.valueOf(5)
        );
        CalculatorProperties properties = new CalculatorProperties(
                BigDecimal.valueOf(20),
                insuranceProps,
                salaryProps
        );
        service = new CalculatorService(properties);
    }
    @Test
    void getOffers_shouldReturn4SortedOffers() {
        var request = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(100_000))
                .term(12)
                .build();

        var offers = service.getAvailableOffers(request);
        assertNotNull(offers);
        assertEquals(4, offers.size());
        assertTrue(offers.get(0).rate().compareTo(offers.get(1).rate()) >= 0);
        assertTrue(offers.get(1).rate().compareTo(offers.get(2).rate()) >= 0);
        assertTrue(offers.get(2).rate().compareTo(offers.get(3).rate()) >= 0);

        var worstOffer = offers.get(0);
        assertFalse(worstOffer.isInsuranceEnabled());
        assertFalse(worstOffer.isSalaryClient());
        assertEquals(BigDecimal.valueOf(20), worstOffer.rate());
        assertEquals(BigDecimal.valueOf(9263.45), worstOffer.monthlyPayment());

        var secondOffer = offers.get(1);
        assertEquals(BigDecimal.valueOf(19), secondOffer.rate());
        assertEquals(BigDecimal.valueOf(9215.66), secondOffer.monthlyPayment());

        var thirdOffer = offers.get(2);
        assertEquals(BigDecimal.valueOf(15), thirdOffer.rate());
        assertEquals(BigDecimal.valueOf(9928.41), thirdOffer.monthlyPayment());

        var bestOffer = offers.get(3);
        assertTrue(bestOffer.isInsuranceEnabled());
        assertTrue(bestOffer.isSalaryClient());
        assertEquals(BigDecimal.valueOf(14), bestOffer.rate());
        assertEquals(BigDecimal.valueOf(11000000, 2), bestOffer.totalAmount());
        assertEquals(BigDecimal.valueOf(9876.58), bestOffer.monthlyPayment());
    }
    @ParameterizedTest
    @MethodSource("initPersonsWithWrongField")
    void calculateCredit_shouldThrowScoringException (ScoringDataDto dto, ScoringError expectedException) {
        var currException = assertThrows(ScoringException.class, () -> service.calculateCredit(dto));
        assertEquals(expectedException.getMessage(), currException.getMessage());
    }
    @Test
    @DisplayName("Успешный расчет кредита: формирование CreditDto, графика и правильный расчет ставки")
    void calculateCredit_ShouldReturnValidCreditDto_WhenDataIsValid() {
        ScoringDataDto validDto = mutate((x) -> x);
        CreditDto result = service.calculateCredit(validDto);

        assertNotNull(result, "CreditDto не должен быть null");

        assertEquals(0, result.amount().compareTo(BigDecimal.valueOf(1100000)),
                "Сумма кредита должна включать 10% страховки");

        assertEquals(0, result.rate().compareTo(BigDecimal.valueOf(8.0)),
                "Итоговая ставка должна быть рассчитана со всеми скидками (5%)");

        assertNotNull(result.monthlyPayment(), "Ежемесячный платеж должен быть рассчитан");
        assertTrue(result.monthlyPayment().compareTo(BigDecimal.ZERO) > 0);

        assertNotNull(result.psk(), "ПСК должен быть рассчитан");
        assertTrue(result.psk().compareTo(BigDecimal.ZERO) > 0);

        List<PaymentScheduleElementDto> schedule = result.paymentSchedule();
        assertNotNull(schedule, "График платежей не должен быть null");
        assertEquals(24, schedule.size(), "График должен состоять ровно из 24 месяцев");

        PaymentScheduleElementDto firstPayment = schedule.get(0);
        assertEquals(1, firstPayment.number(), "Номер первого платежа должен быть 1");
        BigDecimal expectedTotalFirst = firstPayment.debtPayment().add(firstPayment.interestPayment());
        assertEquals(0, firstPayment.totalPayment().compareTo(expectedTotalFirst),
                "TotalPayment должен равняться сумме основного долга и процентов");

        var lastPayment = result.paymentSchedule().get(23);
        assertEquals(firstPayment.interestPayment(), BigDecimal.valueOf(7333.33));
        assertEquals(firstPayment.debtPayment(), BigDecimal.valueOf(42416.69));
        assertEquals(lastPayment.number(), 24);
        assertEquals(lastPayment.interestPayment(), BigDecimal.valueOf(329.47));
        assertEquals(lastPayment.debtPayment(), BigDecimal.valueOf(49420.59));
        assertEquals(lastPayment.remainingDebt(), BigDecimal.valueOf(49420.59));

    }

    private static ScoringDataDto mutate(UnaryOperator<ScoringDataDto> mutator) {
        var dto = ScoringDataDto.builder()
                .amount(BigDecimal.valueOf(1_000_000))
                .term(24)
                .firstName("Ivan")
                .lastName("Ivanov")
                .gender(Gender.MALE)
                .birthdate(LocalDate.now().minusYears(30))
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(0)
                .passportSeries("1234")
                .passportNumber("123456")
                .passportIssueBranch("ОВД")
                .passportIssueDate(LocalDate.now().minusYears(5))
                .accountNumber("40817810099910004312")
                .isInsuranceEnabled(true)
                .isSalaryClient(true)
                .employment(new EmploymentDto(
                        EmploymentStatus.EMPLOYED,
                        "7728168971",
                        BigDecimal.valueOf(100000),
                        Position.WORKER,
                        60,
                        24
                ))
                .build();
        return mutator.apply(dto);
    }
    private static Stream<Arguments> initPersonsWithWrongField() {
        return Stream.of(
                Arguments.of(mutate((x) -> x.toBuilder().birthdate(LocalDate.now()).build())
                        , ScoringError.BAD_AGE),
                Arguments.of(mutate((x) -> x.toBuilder().employment(x.employment().toBuilder().workExperienceCurrent(2).build()).build())
                        , ScoringError.LOW_CURRENT_EXP),
                Arguments.of(mutate((x) -> x.toBuilder().employment(x.employment().toBuilder().salary(BigDecimal.valueOf(100)).build()).build())
                        , ScoringError.LOW_SALARY),
                Arguments.of(mutate((x) -> x.toBuilder().employment(x.employment().toBuilder().workExperienceTotal(10).build()).build())
                        , ScoringError.LOW_TOTAL_EXP),
                Arguments.of(mutate((x) -> x.toBuilder().employment(x.employment().toBuilder().employmentStatus(EmploymentStatus.UNEMPLOYED).build()).build())
                        , ScoringError.UNEMPLOYED)
        );
    }

}