package neoflex.chulkov.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.SesCodeRequestDto;
import neoflex.chulkov.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentApiDelegateImpl implements DocumentApiDelegate {
    private final DocumentService documentService;
    @Override
    public ResponseEntity<Void> sendDocuments(String statementId) {
        log.info("called /deal/document/{statementId}/send with statementId = {}", statementId);
        documentService.sendDocuments(statementId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> signDocuments(String statementId) {
        log.info("called /deal/document/{statementId}/sign with statementId = {}", statementId);
        documentService.signDocument(statementId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> verifySesCode(String statementId, SesCodeRequestDto sesCodeRequestDto) {
        log.info("called /deal/document/{statementId}/code with statementId = {}", statementId);
        documentService.codeDocument(statementId, sesCodeRequestDto.getSes());
        return ResponseEntity.ok().build();
    }
}
