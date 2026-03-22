package neoflex.chulkov.api.deal;


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
