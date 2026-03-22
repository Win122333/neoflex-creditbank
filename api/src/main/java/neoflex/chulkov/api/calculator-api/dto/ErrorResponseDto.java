package neoflex.chulkov.api.calculator;

public record ErrorResponseDto (
        Integer status,
        String error,
        String message
) {
}
