package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.entity.Credit;
import neoflex.chulkov.repository.CreditRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditService {
    private final CreditRepository creditRepository;

    public Credit saveCredit(Credit entityToSave) {
        return creditRepository.save(entityToSave);
    }
}
