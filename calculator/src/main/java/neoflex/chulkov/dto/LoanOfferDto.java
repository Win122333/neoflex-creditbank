package neoflex.chulkov.dto;


import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;
@Builder
public record LoanOfferDto(
        UUID statementId,
        BigDecimal requestedAmount ,
        BigDecimal totalAmount,
        Integer term,
        BigDecimal monthlyPayment,
        BigDecimal rate,
        Boolean isInsuranceEnabled,
        Boolean isSalaryClient
) {
}
