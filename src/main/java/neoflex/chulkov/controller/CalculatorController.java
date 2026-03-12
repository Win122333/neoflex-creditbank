package neoflex.chulkov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.service.CalculatorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calculator")
public class CalculatorController {
    private final CalculatorService calculatorService;
    @PostMapping("/offers")
    public List<LoanOfferDto> getAvailableOffers(
            @Valid @RequestBody LoanStatementRequestDto loanStatementRequestDto
            ) {
        return calculatorService.getAvailableOffers(loanStatementRequestDto);
    }
//    @PostMapping("/calc")
//    public CreditDto calculate(ScoringDataDto scoringDataDto) {
//
//    }
}
