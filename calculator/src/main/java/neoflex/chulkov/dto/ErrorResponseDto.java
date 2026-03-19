package neoflex.chulkov.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ErrorResponseDto (
        Integer status,
        String error,
        String message
) {
}
