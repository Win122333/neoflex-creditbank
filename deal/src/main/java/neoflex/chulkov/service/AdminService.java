package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.StatementDto;
import neoflex.chulkov.exception.StatementNotFoundException;
import neoflex.chulkov.mapper.StatementMapper;
import neoflex.chulkov.repository.StatementRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final StatementMapper statementMapper;
    private final StatementRepository statementRepository;
    public List<StatementDto> getAllStatements() {
        return statementRepository.findAll().stream()
            .map(statementMapper::toDto)
            .toList();
    }
    public StatementDto getStatementById(String id) {
        return statementMapper.toDto(
            statementRepository.findById(UUID.fromString(id)).orElseThrow(
                () -> new StatementNotFoundException(
                    "Заявка не найдена с UUID == %s".formatted(id))
            ));
    }
}
