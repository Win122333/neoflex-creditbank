package neoflex.chulkov.api.calculator;

import neoflex.chulkov.dto.enums.ChangeType;
import neoflex.chulkov.dto.enums.StatementStatus;

import java.time.LocalDateTime;

public record StatementStatusHistoryDto(
        StatementStatus status,
        LocalDateTime time,
        ChangeType changeType
) {
}
