package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.client.deal.DealApi;
import neoflex.chulkov.client.deal.DocumentApi;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.SesCodeRequestDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService {
    private final DealApi dealClient;
    private final DocumentApi documentClient;

    public void finishRegistration(String statementId, FinishRegistrationRequestDto finishRegistrationRequestDto) {
        dealClient.finishRegistration(statementId, finishRegistrationRequestDto);
    }

    public void verifySesCode(String statementId, SesCodeRequestDto sesCodeRequestDto) {
        documentClient.verifySesCode(statementId, sesCodeRequestDto);
    }

    public void signDocuments(String statementId) {
        documentClient.signDocuments(statementId);
    }

    public void sendDocuments(String statementId) {
        documentClient.sendDocuments(statementId);
    }
}
