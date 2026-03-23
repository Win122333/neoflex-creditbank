package neoflex.chulkov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.LoanOfferDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.service.DealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/deal")
@RequiredArgsConstructor
public class DealController {
    private final DealService dealService;

    @PostMapping("/statement")
    public ResponseEntity<List<LoanOfferDto>> statement(
            @RequestBody @Valid LoanStatementRequestDto dto
    ) {
        log.info("called /statement with dto = {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.createStatement(dto));
    }
    @PostMapping("/offer/select")
    public ResponseEntity<Void> select(
            @RequestBody LoanOfferDto dto
    ) {
        log.info("called /offer/select with dto = {}", dto);
        dealService.selectOffer(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
    @PostMapping("/calculate/{statementId}")
    public ResponseEntity<Void> calculate(
            @RequestBody FinishRegistrationRequestDto dto,
            @PathVariable String statementId
    ) {
        log.info("called /calculate/{statementId} with dto = {}", dto);
        dealService.calculateCredit(dto, statementId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
