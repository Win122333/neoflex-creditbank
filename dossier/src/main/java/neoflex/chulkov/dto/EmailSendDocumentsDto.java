package neoflex.chulkov.dto;

public record EmailSendDocumentsDto (
        String firstName,
        String lastName,
        String middleName,
        String email,
        String statementId,
        CreditDto creditDto
) {

}
