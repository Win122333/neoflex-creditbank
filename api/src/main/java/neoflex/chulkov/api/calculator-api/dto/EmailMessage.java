package neoflex.chulkov.api.calculator;

import neoflex.chulkov.dto.enums.EmailTheme;

public record EmailMessage(
        String address,
        EmailTheme theme,
        Long statementId,
        String text
) {
}
