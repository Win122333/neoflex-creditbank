package neoflex.chulkov.dto;

public record ErrorResponseDto (
        Integer status,
        String error,
        String message
) {
}
