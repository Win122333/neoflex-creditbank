package neoflex.chulkov.mapper;

import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.ScoringDataDto;
import neoflex.chulkov.entity.Statement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScoringDataMapper {

    @Mapping(target = "amount", source = "statement.appliedOffer.requestedAmount")
    @Mapping(target = "term", source = "statement.appliedOffer.term")
    @Mapping(target = "isInsuranceEnabled", source = "statement.appliedOffer.isInsuranceEnabled")
    @Mapping(target = "isSalaryClient", source = "statement.appliedOffer.isSalaryClient")
    @Mapping(target = "firstName", source = "statement.client.firstName")
    @Mapping(target = "lastName", source = "statement.client.lastName")
    @Mapping(target = "middleName", source = "statement.client.middleName")
    @Mapping(target = "birthdate", source = "statement.client.birthDate")
    @Mapping(target = "passportSeries", source = "statement.client.passport.series")
    @Mapping(target = "passportNumber", source = "statement.client.passport.number")
    @Mapping(target = "passportIssueBranch", source = "finishDto.passportIssueBranch")
    @Mapping(target = "passportIssueDate", source = "finishDto.passportIssueDate")
    @Mapping(target = "gender", source = "finishDto.gender")
    @Mapping(target = "maritalStatus", source = "finishDto.maritalStatus")
    @Mapping(target = "accountNumber", source = "finishDto.accountNumber")
    @Mapping(target = "employment", source = "finishDto.employment")
    @Mapping(target = "dependentAmount", source = "finishDto.dependentAmount")
    ScoringDataDto toScoringDataDto(Statement statement, FinishRegistrationRequestDto finishDto);
}
