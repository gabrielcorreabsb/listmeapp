package com.listme.controller;

import com.listme.dto.MessageResponse;
import com.listme.dto.OrcamentoRequestDTO;
import com.listme.dto.OrcamentoResponseDTO;
import com.listme.model.Orcamento;
import com.listme.service.OrcamentoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Para controle de acesso
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    @PostMapping
    public ResponseEntity<OrcamentoResponseDTO> criar(@Valid @RequestBody OrcamentoRequestDTO orcamentoRequestDTO) {
        OrcamentoResponseDTO novoOrcamento = orcamentoService.criarOrcamento(orcamentoRequestDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(novoOrcamento.getId())
                .toUri();
        return ResponseEntity.created(location).body(novoOrcamento);
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(orcamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(orcamentoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrcamentoResponseDTO> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody OrcamentoRequestDTO orcamentoRequestDTO) {
        return ResponseEntity.ok(orcamentoService.atualizarOrcamento(id, orcamentoRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        orcamentoService.deletarOrcamento(id);
        return ResponseEntity.noContent().build();
    }

    // Adicionar endpoint para enviar por email
    @PostMapping("/{id}/enviar-email")
    public ResponseEntity<Void> enviarOrcamentoPorEmail(@PathVariable Long id) {
        // Lógica no OrcamentoService para buscar o orçamento, gerar um PDF/HTML,
        // obter o email do cliente e enviar.
        // orcamentoService.enviarPorEmail(id);
        // Esta é uma funcionalidade mais complexa que pode envolver templates de email e
        // configuração de um serviço de envio de email (ex: Spring Mail).
        // Por agora, vamos deixar o endpoint e você pode implementar a lógica depois.
        // Aqui, vamos apenas simular o sucesso.
        System.out.println("Simulando envio de email para orçamento ID: " + id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDEDOR')") // Ou a permissão apropriada
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestParam("status") String novoStatusStr) {
        try {
            // Converter a string do status para o Enum
            Orcamento.StatusOrcamento novoStatusEnum;
            try {
                novoStatusEnum = Orcamento.StatusOrcamento.valueOf(novoStatusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new MessageResponse("Status inválido: " + novoStatusStr));
            }

            OrcamentoResponseDTO orcamentoAtualizado = orcamentoService.atualizarStatusOrcamento(id, novoStatusEnum);
            return ResponseEntity.ok(orcamentoAtualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (IllegalStateException e) { // Para regras de negócio, ex: não pode mudar de PAGO para PENDENTE
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            // Logar o erro
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Erro ao atualizar status do orçamento."));
        }
    }
}