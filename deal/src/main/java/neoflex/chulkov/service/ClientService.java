package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.exception.EmailAlreadyExistsException;
import neoflex.chulkov.mapper.ClientMapper;
import neoflex.chulkov.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public Client createClient(LoanStatementRequestDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail()))
            throw new EmailAlreadyExistsException("Клиент с данным email %s уже существует".formatted(dto.getEmail()));
        Client client = clientMapper.toClient(dto);
        log.debug("save client {}", client);
        return saveClient(client);
    }

    public Client saveClient(Client entityToSave) {
        log.info("Client saved = {}", entityToSave);
        return clientRepository.save(entityToSave);
    }

    public void updateClient(Client entityToUpdate, FinishRegistrationRequestDto dto) {
        clientMapper.updateClientFromDto(dto, entityToUpdate);
        log.info("Client updated = {}", entityToUpdate);
        clientRepository.save(entityToUpdate);
    }
}
