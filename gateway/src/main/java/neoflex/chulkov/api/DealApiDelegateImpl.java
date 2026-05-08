package neoflex.chulkov.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.SesCodeRequestDto;
import neoflex.chulkov.service.DealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealApiDelegateImpl implements DealApiDelegate {
    private final DealService dealService;

    @Override
    public ResponseEntity<Void> sendDocuments(String statementId) {
        dealService.sendDocuments(statementId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<Void> signDocuments(String statementId) {
        dealService.signDocuments(statementId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<Void> verifySesCode(String statementId, SesCodeRequestDto sesCodeRequestDto) {
        dealService.verifySesCode(statementId, sesCodeRequestDto);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void
        > finishRegistration(
        String statementId,
        FinishRegistrationRequestDto finishRegistrationRequestDto
    ) {
        dealService.finishRegistration(statementId, finishRegistrationRequestDto);
        return ResponseEntity.ok().build();
    }
}
