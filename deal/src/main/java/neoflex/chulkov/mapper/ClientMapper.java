package neoflex.chulkov.mapper;

import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    @Mapping(target = "passport.series", source = "passportSeries")
    @Mapping(target = "passport.number", source = "passportNumber")
    @Mapping(target = "birthDate", source = "birthday")
    Client toClient(LoanStatementRequestDto loanStatement);
}