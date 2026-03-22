package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.StatementNotFoundException;
import neoflex.chulkov.repository.StatementRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatementService {
    private final StatementRepository statementRepository;

    public Statement save(Statement entityToSave) {
        return statementRepository.save(entityToSave);
    }

    public Statement getStatementById(UUID statementId) {
        return statementRepository.findById(statementId)
                .orElseThrow(() -> new StatementNotFoundException("Заявка не найдена с UUID == " + statementId));
    }
}
