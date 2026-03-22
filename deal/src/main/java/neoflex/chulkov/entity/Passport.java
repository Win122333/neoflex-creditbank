package neoflex.chulkov.entity;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record Passport(
        UUID passportUUID,
        String series,
        String number,
        String issueBranch,
        LocalDateTime issueDate
) {
}
