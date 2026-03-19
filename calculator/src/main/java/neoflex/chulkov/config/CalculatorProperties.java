package neoflex.chulkov.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "calculator")
public record CalculatorProperties (
        BigDecimal rate,
        Insurance insurance,
        Salary salary
) {
    public record Insurance(
            BigDecimal costInPercent,
            BigDecimal discount
    ) {}

    public record Salary(
            BigDecimal discount
    ) {}
}
