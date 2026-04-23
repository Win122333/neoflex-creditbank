package neoflex.chulkov.mapper;

import neoflex.chulkov.dto.EmailMessage;
import neoflex.chulkov.entity.Client;
import org.springframework.stereotype.Component;

@Component
public class EmailMessageMapper {
    public EmailMessage createEmailMessageDto(
            Client client,
            String statementId
    ) {
        return new EmailMessage()
                .email(client.getEmail())
                .birthday(client.getBirthday())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .middleName(client.getMiddleName())
                .statementId(statementId);
    }
}
