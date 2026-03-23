package neoflex.chulkov.mapper;

import neoflex.chulkov.dto.EmploymentDto;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Employment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    @Mapping(target = "passport.series", source = "passportSeries")
    @Mapping(target = "passport.number", source = "passportNumber")
    @Mapping(target = "birthDate", source = "birthday")
    Client toClient(LoanStatementRequestDto loanStatement);

    @Mapping(target = "passport.issueDate", source = "passportIssueDate")
    @Mapping(target = "passport.issueBranch", source = "passportIssueBranch")
    @Mapping(target = "employment", source = "employment")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateClientFromDto(FinishRegistrationRequestDto dto, @MappingTarget Client client);

    Employment mapEmployment(EmploymentDto value);
}