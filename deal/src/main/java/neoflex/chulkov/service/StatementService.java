package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.repository.StatementRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatementService {
    private final StatementRepository statementRepository;

    public Statement save(Statement entityToSave) {
        return statementRepository.save(entityToSave);
    }
}
