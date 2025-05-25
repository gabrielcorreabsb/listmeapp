package com.listme.controller;

import com.listme.dto.OrcamentoRequestDTO;
import com.listme.dto.OrcamentoResponseDTO;
import com.listme.service.OrcamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}