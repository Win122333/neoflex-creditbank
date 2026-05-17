package neoflex.chulkov.dto;

public record CreditIssuedDto(
        String email,
        String firstName,
        String lastName,
        String statementId
) {
}
