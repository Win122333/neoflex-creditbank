package neoflex.chulkov.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.StatementDto;
import neoflex.chulkov.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminApiDelegateImpl implements AdminApiDelegate {
    private final AdminService adminService;
    @Override
    public ResponseEntity<List<StatementDto>> getAllStatements() {
        log.info("called /deal/admin/statement");
        return ResponseEntity.ok(adminService.getAllStatements());
    }

    @Override
    public ResponseEntity<StatementDto> getStatementById(String statementId) {
        log.info("called /deal/admin/statement/{statementId} with statementId = {}", statementId);
        return ResponseEntity.ok(adminService.getStatementById(statementId));
    }
}
