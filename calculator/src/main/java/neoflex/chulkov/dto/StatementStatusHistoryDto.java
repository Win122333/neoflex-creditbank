package neoflex.chulkov.dto;

import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.dto.enums.ChangeType;

import java.time.LocalDateTime;

public record StatementStatusHistoryDto(
        ApplicationStatus status,
        LocalDateTime time,
        ChangeType changeType
) {
}
