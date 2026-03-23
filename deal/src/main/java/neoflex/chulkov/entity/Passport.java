package neoflex.chulkov.entity;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class Passport {
    private UUID passportUUID;
    private String series;
    private String number;
    private String issueBranch;
    private LocalDateTime issueDate;
}
