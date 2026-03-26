package neoflex.chulkov.service;

import neoflex.chulkov.dto.FinishRegistrationRequestDto;
import neoflex.chulkov.dto.LoanStatementRequestDto;
import neoflex.chulkov.entity.Client;
import neoflex.chulkov.mapper.ClientMapper;
import neoflex.chulkov.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    @Test
    @DisplayName("Создание клиента: маппер превращает DTO в сущность, а репозиторий ее сохраняет")
    void createClient_ShouldMapAndSaveClient() {
        LoanStatementRequestDto dto = new LoanStatementRequestDto();
        Client mappedClient = new Client();
        Client savedClient = new Client();

        when(clientMapper.toClient(dto)).thenReturn(mappedClient);
        when(clientRepository.save(mappedClient)).thenReturn(savedClient);

        Client result = clientService.createClient(dto);

        assertEquals(savedClient, result);

        verify(clientMapper).toClient(dto);
        verify(clientRepository).save(mappedClient);
    }

    @Test
    @DisplayName("Сохранение клиента: вызывается метод save у репозитория")
    void saveClient_ShouldCallRepositorySave() {
        Client clientToSave = new Client();
        Client savedClient = new Client();

        when(clientRepository.save(clientToSave)).thenReturn(savedClient);

        Client result = clientService.saveClient(clientToSave);

        assertEquals(savedClient, result);
        verify(clientRepository).save(clientToSave);
    }

    @Test
    @DisplayName("Обновление клиента: маппер обновляет поля, репозиторий сохраняет")
    void updateClient_ShouldUpdateViaMapperAndSave() {
        Client existingClient = new Client();
        FinishRegistrationRequestDto dto = new FinishRegistrationRequestDto();

        clientService.updateClient(existingClient, dto);

        verify(clientMapper).updateClientFromDto(dto, existingClient);
        verify(clientRepository).save(existingClient);
    }
}