package com.listme.service;

import com.listme.dto.ClienteDTO;
import com.listme.model.Cliente;
import com.listme.repository.ICliente;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ICliente clienteRepository;

    /**
     * Lista todos os clientes cadastrados
     * @return Lista de DTOs de clientes
     */
    public List<ClienteDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Busca um cliente pelo ID
     * @param id ID do cliente
     * @return DTO do cliente encontrado
     * @throws EntityNotFoundException se o cliente não for encontrado
     */
    public ClienteDTO buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .map(ClienteDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
    }

    /**
     * Cria um novo cliente
     * @param clienteDTO DTO com os dados do cliente
     * @return DTO do cliente criado
     */
    @Transactional
    public ClienteDTO criar(ClienteDTO clienteDTO) {
        Cliente cliente = clienteDTO.toEntity();
        cliente = clienteRepository.save(cliente);
        return ClienteDTO.fromEntity(cliente);
    }

    /**
     * Atualiza um cliente existente
     * @param id ID do cliente a ser atualizado
     * @param clienteDTO DTO com os novos dados do cliente
     * @return DTO do cliente atualizado
     * @throws EntityNotFoundException se o cliente não for encontrado
     */
    @Transactional
    public ClienteDTO atualizar(Long id, ClienteDTO clienteDTO) {
        if (!clienteRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente não encontrado");
        }

        Cliente cliente = clienteDTO.toEntity();
        cliente.setId(id);
        cliente = clienteRepository.save(cliente);
        return ClienteDTO.fromEntity(cliente);
    }

    /**
     * Remove um cliente
     * @param id ID do cliente a ser removido
     * @throws EntityNotFoundException se o cliente não for encontrado
     */
    @Transactional
    public void deletar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente não encontrado");
        }
        clienteRepository.deleteById(id);
    }
}