package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.client.DealRestClient;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.SesCodeRequestDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService {
    private final DealRestClient dealRestClient;

    public void finishRegistration(String statementId, FinishRegistrationRequestDto finishRegistrationRequestDto) {
        dealRestClient.finishRegistration(finishRegistrationRequestDto, statementId);
    }

    public void verifySesCode(String statementId, SesCodeRequestDto sesCodeRequestDto) {
        dealRestClient.verifySesCode(statementId, sesCodeRequestDto);
    }

    public void signDocuments(String statementId) {
        dealRestClient.signDocuments(statementId);
    }

    public void sendDocuments(String statementId) {
        dealRestClient.sendDocuments(statementId);
    }
}
