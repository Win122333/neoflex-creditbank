package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
class CalculatorServiceTest {
    private CalculatorService service;
    @BeforeEach
    void setUp() {
        service = new CalculatorService();
        ReflectionTestUtils.setField(service, "baseRate", BigDecimal.valueOf(20));
        ReflectionTestUtils.setField(service, "salaryDiscount", BigDecimal.valueOf(1));
        ReflectionTestUtils.setField(service, "insuranceDiscount", BigDecimal.valueOf(5));
        ReflectionTestUtils.setField(service, "insuranceCostInPercent", 0.10);
    }
    @Test
    void getOffers_shouldReturn4SortedOffers() {
        var request = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(100_000))
                .term(12)
                .build();

        var offers = service.getAvailableOffers(request);
        assertNotNull(offers);
        assertEquals(4, offers.size());
        assertTrue(offers.get(0).rate().compareTo(offers.get(1).rate()) >= 0);
        assertTrue(offers.get(1).rate().compareTo(offers.get(2).rate()) >= 0);
        assertTrue(offers.get(2).rate().compareTo(offers.get(3).rate()) >= 0);

        var worstOffer = offers.get(0);
        assertFalse(worstOffer.isInsuranceEnabled());
        assertFalse(worstOffer.isSalaryClient());
        assertEquals(BigDecimal.valueOf(20), worstOffer.rate());
        assertEquals(BigDecimal.valueOf(9263.45), worstOffer.monthlyPayment());

        var secondOffer = offers.get(1);
        assertEquals(BigDecimal.valueOf(19), secondOffer.rate());
        assertEquals(BigDecimal.valueOf(9215.66), secondOffer.monthlyPayment());

        var thirdOffer = offers.get(2);
        assertEquals(BigDecimal.valueOf(15), thirdOffer.rate());
        assertEquals(BigDecimal.valueOf(9928.41), thirdOffer.monthlyPayment());

        var bestOffer = offers.get(3);
        assertTrue(bestOffer.isInsuranceEnabled());
        assertTrue(bestOffer.isSalaryClient());
        assertEquals(BigDecimal.valueOf(14), bestOffer.rate());
        assertEquals(BigDecimal.valueOf(11000000, 2), bestOffer.totalAmount());
        assertEquals(BigDecimal.valueOf(9876.58), bestOffer.monthlyPayment());
    }
}