package neoflex.chulkov.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.SesCodeRequestDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealRestClient {
    private final RestClient restClient;

    public void finishRegistration(
        FinishRegistrationRequestDto finishRegistrationRequestDto,
        String statementId
    ) {
        log.info("sending request to deal service finish registration with statementId {}", statementId);
        restClient.post()
            .uri("/deal/calculate/{statementId}", statementId)
            .body(finishRegistrationRequestDto).contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .toBodilessEntity();
    }

    public void sendDocuments(String statementId) {
        log.info("sending request to deal service to generate documents with statementId {}", statementId);
        restClient.post()
            .uri("/deal/document/{statementId}/send", statementId)
            .retrieve()
            .toBodilessEntity();
    }

    public void signDocuments(String statementId) {
        log.info("sending request to deal service to sign documents with statementId {}", statementId);
        restClient.post()
            .uri("/deal/document/{statementId}/sign", statementId)
            .retrieve()
            .toBodilessEntity();
    }

    public void verifySesCode(String statementId, SesCodeRequestDto sesCodeRequestDto) {
        log.info("sending request to deal service to check ses with statementId {}", statementId);
        restClient.post()
            .uri("/deal/document/{statementId}/code", statementId)
            .body(sesCodeRequestDto).contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .toBodilessEntity();
    }
}
