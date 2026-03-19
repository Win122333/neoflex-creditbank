package neoflex.chulkov.dto;

import neoflex.chulkov.dto.enums.EmailTheme;

public record EmailMessage(
        String address,
        EmailTheme theme,
        Long statementId,
        String text
) {
}
