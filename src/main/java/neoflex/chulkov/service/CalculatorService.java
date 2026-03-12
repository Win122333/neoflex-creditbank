package neoflex.chulkov.service;

import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CalculatorService {
    @Value("${calculator.insurance.costInPercent:0.10}")
    private Double insuranceCostInPercent;
    @Value("${calculator.insurance.discount:2}")
    private BigDecimal insuranceDiscount;
    @Value("${calculator.salary.discount:1}")
    private BigDecimal salaryDiscount;
    @Value("${calculator.rate:20}")
    private BigDecimal baseRate;


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
        BigDecimal rate = calculateRate(isInsuranceEnabled, isSalaryClient);
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
        return amount.multiply(BigDecimal.valueOf(insuranceCostInPercent)).setScale(2, RoundingMode.HALF_UP);
    }
    private BigDecimal calculateRate(boolean isInsuranceEnabled, boolean isSalaryClient) {
        BigDecimal rate = baseRate;
        if (isInsuranceEnabled) {
            rate = rate.subtract(insuranceDiscount);
        }
        if (isSalaryClient) {
            rate = rate.subtract(salaryDiscount);
        }
        return rate;
    }
    private BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return amount.divide(BigDecimal.valueOf(term), 2, RoundingMode.HALF_UP);
        }
        BigDecimal monthlyRate = rate.divide(
                BigDecimal.valueOf(12 * 100),
                10,
                RoundingMode.HALF_UP
        );

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powFactor = onePlusRate.pow(term);

        BigDecimal annuityCoefficient = monthlyRate
                .multiply(powFactor)
                .divide(powFactor.subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);

        return amount
                .multiply(annuityCoefficient)
                .setScale(2, RoundingMode.HALF_UP);
    }
}