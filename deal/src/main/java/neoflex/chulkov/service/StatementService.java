package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.enums.ApplicationStatus;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.entity.Statement;
import neoflex.chulkov.exception.StatementNotFoundException;
import neoflex.chulkov.repository.StatementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementService {
    private final StatementRepository statementRepository;

    public Statement createStatement(Client client) {
        Statement statement = new Statement()
                .setClient(client)
                .setStatus(ApplicationStatus.PREAPPROVAL)
                .setCreationDate(LocalDateTime.now());
        log.debug("create statement = {}", statement);
        return saveStatement(statement);
    }

    public Statement saveStatement(Statement entityToSave) {
        return statementRepository.save(entityToSave);
    }

    public Statement getStatementById(UUID statementId) {
        return statementRepository.findById(statementId)
                .orElseThrow(() -> new StatementNotFoundException("Заявка не найдена с UUID == " + statementId));
    }
}
