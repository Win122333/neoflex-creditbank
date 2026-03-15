package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.config.CalculatorProperties;
import neoflex.chulkov.dto.*;
import neoflex.chulkov.dto.enums.*;
import neoflex.chulkov.exception.ScoringException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorService {
    private final CalculatorProperties properties;
    private static final int SCALE_INTERMEDIATE = 10;
    private static final int SCALE_MONEY = 2;
    private static final int SCALE_PSK = 4;
    private static final int MONTHS_IN_YEAR = 12;
    private static final int PERCENT_DIVISOR = 100;


    public List<LoanOfferDto> getAvailableOffers(LoanStatementRequestDto req) {
        List<LoanOfferDto> offers = new ArrayList<>(4);
        offers.add(createOffer(false, false, req));
        offers.add(createOffer(false, true, req));
        offers.add(createOffer(true, false, req));
        offers.add(createOffer(true, true, req));
        offers.sort(Comparator.comparing(LoanOfferDto::rate).reversed());

        return offers;
    }
    private LoanOfferDto createOffer(
            Boolean isInsuranceEnabled,
            Boolean isSalaryClient,
            LoanStatementRequestDto req

    ) {
        BigDecimal insuranceCost = calculateInsuranceCost(req.amount(), isInsuranceEnabled);
        BigDecimal totalAmount = req.amount().add(insuranceCost);
        BigDecimal rate = properties.rate();
        if (isInsuranceEnabled) {
            rate = rate.subtract(properties.insurance().discount());
        }
        if (isSalaryClient) {
            rate = rate.subtract(properties.salary().discount());
        }
        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, req.term(), rate);

        return LoanOfferDto.builder()
                .isSalaryClient(isSalaryClient)
                .isInsuranceEnabled(isInsuranceEnabled)
                .monthlyPayment(monthlyPayment)
                .rate(rate)
                .totalAmount(totalAmount)
                .requestedAmount(req.amount())
                .term(req.term())
                .build();

    }
    private BigDecimal calculateInsuranceCost(BigDecimal amount, boolean isInsuranceEnabled) {
        if (!isInsuranceEnabled) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(properties.insurance().costInPercent()).setScale(2, RoundingMode.HALF_UP);
    }
    private BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal rate) {
        log.debug("Calculating monthly payment: amount={}, term={}, rate={}", amount, term, rate);

        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return amount.divide(BigDecimal.valueOf(term), SCALE_MONEY, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = rate.divide(
                BigDecimal.valueOf(MONTHS_IN_YEAR * PERCENT_DIVISOR),
                SCALE_INTERMEDIATE,
                RoundingMode.HALF_UP
        );

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powFactor = onePlusRate.pow(term);

        BigDecimal annuityCoefficient = monthlyRate
                .multiply(powFactor)
                .divide(powFactor.subtract(BigDecimal.ONE), SCALE_INTERMEDIATE, RoundingMode.HALF_UP);

        BigDecimal exactPayment = amount.multiply(annuityCoefficient);

        return exactPayment.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }
    public CreditDto calculateCredit(ScoringDataDto scoringDataDto) {
        performScoring(scoringDataDto);
        BigDecimal rate = calculateRate(scoringDataDto);
        BigDecimal psk = calculatePsk(scoringDataDto.term(), rate);
        BigDecimal amount = scoringDataDto.amount()
                .add(calculateInsuranceCost(scoringDataDto.amount(), scoringDataDto.isInsuranceEnabled()));
        BigDecimal monthlyPayment = calculateMonthlyPayment(amount, scoringDataDto.term(), rate);
        var schedulePayment = calculateSchedulePayment(amount, scoringDataDto.term(), rate, monthlyPayment);
        return new CreditDto(
                amount,
                scoringDataDto.term(),
                monthlyPayment,
                rate,
                psk,
                scoringDataDto.isInsuranceEnabled(),
                scoringDataDto.isSalaryClient(),
                schedulePayment
        );
    }
    private BigDecimal calculateRate(ScoringDataDto applicant) {
        int age = Period.between(applicant.birthdate(), LocalDate.now()).getYears();
        BigDecimal rate = properties.rate();
        EmploymentDto employer = applicant.employment();

        if (applicant.isInsuranceEnabled())
            rate = rate.subtract(properties.insurance().discount());
        if (applicant.isSalaryClient())
            rate = rate.subtract(properties.salary().discount());
        //employment status
        if (employer.employmentStatus() == EmploymentStatus.SELF_EMPLOYED)
            rate = rate.add(BigDecimal.valueOf(2));
        if (employer.employmentStatus() == EmploymentStatus.BUSINESS_OWNER)
            rate = rate.add(BigDecimal.valueOf(1));
        //position
        if (employer.position() == Position.MIDDLE_MANAGER)
            rate = rate.subtract(BigDecimal.valueOf(2));
        if (employer.position() == Position.TOP_MANAGER)
            rate = rate.subtract(BigDecimal.valueOf(3));
        //marital status
        if (applicant.maritalStatus() == MaritalStatus.MARRIED)
            rate = rate.subtract(BigDecimal.valueOf(3));
        if (applicant.maritalStatus() == MaritalStatus.DIVORCED)
            rate = rate.add(BigDecimal.ONE);
        //gender
        if (applicant.gender() == Gender.FEMALE && age >= 32 && age <= 60)
            rate = rate.subtract(BigDecimal.valueOf(3));
        if (applicant.gender() == Gender.MALE && age >= 30 && age <= 55)
            rate = rate.subtract(BigDecimal.valueOf(3));
        if (applicant.gender() == Gender.NON_BINARY)
            rate = rate.add(BigDecimal.valueOf(7));

        return rate;
    }
    private void performScoring(ScoringDataDto applicant) {
        int age = Period.between(applicant.birthdate(), LocalDate.now()).getYears();
        EmploymentDto employer = applicant.employment();

        //REJECTS
        if (employer.employmentStatus() == EmploymentStatus.UNEMPLOYED)
            throw new ScoringException(ScoringError.UNEMPLOYED);
        if (applicant.amount().compareTo(employer.salary().multiply(BigDecimal.valueOf(24))) > 0)
            throw new ScoringException(ScoringError.LOW_SALARY);
        if (age < 20 || age > 65)
            throw new ScoringException(ScoringError.BAD_AGE);
        if (employer.workExperienceTotal() < 18)
            throw new ScoringException(ScoringError.LOW_TOTAL_EXP);
        if (employer.workExperienceCurrent() < 3)
            throw new ScoringException(ScoringError.LOW_CURRENT_EXP);
    }
    private BigDecimal calculatePsk(int term, BigDecimal rate) {
        System.out.println();
        BigDecimal monthlyRate = rate.divide(
                BigDecimal.valueOf(MONTHS_IN_YEAR * PERCENT_DIVISOR),
                SCALE_INTERMEDIATE,
                RoundingMode.HALF_UP
        );
        BigDecimal psk = monthlyRate
                .multiply(BigDecimal.valueOf(term))
                .multiply(BigDecimal.valueOf(100));

        return psk.setScale(SCALE_PSK, RoundingMode.HALF_UP);
    }
    private List<PaymentScheduleElementDto> calculateSchedulePayment(
            BigDecimal amount,
            Integer term,
            BigDecimal rate,
            BigDecimal monthlyPayment
    ) {
        List<PaymentScheduleElementDto> schedulePayment = new ArrayList<>(term);
        BigDecimal remainingDebt = amount;
        BigDecimal monthlyRate = rate.divide(
                BigDecimal.valueOf(MONTHS_IN_YEAR * PERCENT_DIVISOR),
                SCALE_INTERMEDIATE,
                RoundingMode.HALF_UP
        );

        LocalDate paymentDate = LocalDate.now();
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalDebt = BigDecimal.ZERO;

        for (int i = 1; i <= term; i++) {
            paymentDate = paymentDate.plusMonths(1);

            BigDecimal exactInterestPayment = remainingDebt.multiply(monthlyRate);
            BigDecimal interestPayment = exactInterestPayment.setScale(SCALE_MONEY, RoundingMode.HALF_UP);

            BigDecimal exactDebtPayment;
            BigDecimal paymentForThisMonth;

            if (i < term) {
                exactDebtPayment = monthlyPayment.subtract(interestPayment);
                paymentForThisMonth = monthlyPayment;
            } else {
                exactDebtPayment = remainingDebt;
                paymentForThisMonth = interestPayment.add(exactDebtPayment);
            }

            BigDecimal debtPayment = exactDebtPayment.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
            BigDecimal newRemainingDebt = remainingDebt.subtract(exactDebtPayment);

            if (i == term) {
                newRemainingDebt = BigDecimal.ZERO;
            }
            totalInterest = totalInterest.add(interestPayment);
            totalDebt = totalDebt.add(debtPayment);

            schedulePayment.add(
                    new PaymentScheduleElementDto(
                            i,
                            paymentDate,
                            paymentForThisMonth.setScale(SCALE_MONEY, RoundingMode.HALF_UP),
                            interestPayment,
                            debtPayment,
                            remainingDebt
                    )
            );

            remainingDebt = newRemainingDebt;
        }

        log.debug("Schedule total: paymentSum={}, interestSum={}, debtSum={}, amount={}",
                totalInterest.add(totalDebt), totalInterest, totalDebt, amount);

        return schedulePayment;
    }
}