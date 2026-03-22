package neoflex.chulkov.mapper;

import neoflex.chulkov.dto.CreditDto;
import neoflex.chulkov.entity.Credit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditMapper {
    @Mapping(target = "insuranceEnabled", source = "isInsuranceEnabled")
    @Mapping(target = "salaryClient", source = "isSalaryClient")
    Credit toCredit(CreditDto creditDto);

    @Mapping(target = "isInsuranceEnabled", source = "insuranceEnabled")
    @Mapping(target = "isSalaryClient", source = "salaryClient")
    CreditDto toCreditDto(Credit credit);
}