package neoflex.chulkov.mapper;

import neoflex.chulkov.dto.EmailMessage;
import neoflex.chulkov.entity.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmailMessageMapper {
    EmailMessage messageFromClient(Client client);
}
