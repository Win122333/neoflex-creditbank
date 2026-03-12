package neoflex.chulkov.service;

import neoflex.chulkov.dto.CreditDto;
import neoflex.chulkov.dto.LoanOfferDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalculatorService {
    public List<LoanOfferDto> getAvailableOffers(  ) {
        List<LoanOfferDto> availableOffers = new ArrayList<>(4);
        return null;
    }
}
