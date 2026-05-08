package neoflex.chulkov.mapper;

import neoflex.chulkov.dto.StatementDto;
import neoflex.chulkov.entity.Statement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StatementMapper {
    @Mapping(target = "clientId", source = "client.clientId")
    @Mapping(target = "creditId", source = "credit.creditId")
    StatementDto toDto(Statement statement);
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "credit", ignore = true)
    Statement toEntity(StatementDto statementDto);
}
