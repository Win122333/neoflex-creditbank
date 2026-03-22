package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.mapper.ClientMapper;
import neoflex.chulkov.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public Client createClient(LoanStatementRequestDto dto) {
        Client client = clientMapper.toClient(dto);
        log.debug("save client {}", client);
        return save(client);
    }

    public Client save(Client entityToSave) {
        return clientRepository.save(entityToSave);
    }
}
