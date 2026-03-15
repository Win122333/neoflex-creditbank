package neoflex.chulkov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.CreditDto;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.dto.ScoringDataDto;
import neoflex.chulkov.service.CalculatorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/calculator")
public class CalculatorController {
    private final CalculatorService calculatorService;

    @PostMapping("/offers")
    public List<LoanOfferDto> getAvailableOffers(
            @Valid @RequestBody LoanStatementRequestDto loanStatementRequestDto
            ) {
        log.info("called /calculator/offers with loanStatementRequestDto = {}", loanStatementRequestDto);
        return calculatorService.getAvailableOffers(loanStatementRequestDto);
    }
    @PostMapping("/calc")
    public CreditDto calculate(
            @Valid @RequestBody ScoringDataDto scoringDataDto
    ) {
        return calculatorService.calculateCredit(scoringDataDto);
    }
}
