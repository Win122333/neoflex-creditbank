package neoflex.chulkov.controller;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.service.DealService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deal")
@RequiredArgsConstructor
public class DealController {
    private final DealService dealService;

    @PostMapping("/statement")
    public List<LoanOfferDto> statement(LoanStatementRequestDto dto) {
        return dealService.statement(dto);
    }
    @PostMapping("/offer/select")
    public void select(LoanOfferDto dto) {
        dealService.select(dto);
    }
    @PostMapping("/calculate/{statementId}")
    public void calculate(
            FinishRegistrationRequestDto dto,
            @RequestParam String statementId
    ) {

    }
}
