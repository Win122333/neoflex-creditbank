package neoflex.chulkov.service;

import lombok.RequiredArgsConstructor;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public Client save(Client entityToSave) {
        return clientRepository.save(entityToSave);
    }
}
