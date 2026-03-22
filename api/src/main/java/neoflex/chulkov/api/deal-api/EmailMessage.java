package neoflex.chulkov.api.deal;

import neoflex.chulkov.dto.enums.EmailTheme;

public record EmailMessage(
        String address,
        EmailTheme theme,
        Long statementId,
        String text
) {
}
