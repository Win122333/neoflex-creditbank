package neoflex.chulkov.api.deal;

public record ErrorResponseDto (
        Integer status,
        String error,
        String message
) {
}
