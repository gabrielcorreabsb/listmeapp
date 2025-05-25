package com.listme.dto;

import com.listme.model.Orcamento;
import com.listme.model.Orcamento.StatusOrcamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoResponseDTO {
    private Long id;
    private ClienteDTO cliente; // Detalhes do cliente
    private UserDTO funcionario; // Detalhes do funcionário (UserDTO que você já tem)
    private LocalDateTime dataOrcamento;
    private List<ItemOrcamentoDTO> itens;
    private BigDecimal valorTotal;
    private String formaPagamento;
    private String observacoes;
    private StatusOrcamento status;

    public static OrcamentoResponseDTO fromEntity(Orcamento orcamento) {
        return OrcamentoResponseDTO.builder()
                .id(orcamento.getId())
                .cliente(ClienteDTO.fromEntity(orcamento.getCliente())) // Converte Cliente para ClienteDTO
                .funcionario(new UserDTO(orcamento.getFuncionario())) // Converte Usuario para UserDTO
                .dataOrcamento(orcamento.getDataOrcamento())
                .itens(orcamento.getItens().stream().map(ItemOrcamentoDTO::fromEntity).collect(Collectors.toList()))
                .valorTotal(orcamento.getValorTotal())
                .formaPagamento(orcamento.getFormaPagamento())
                .observacoes(orcamento.getObservacoes())
                .status(orcamento.getStatus())
                .build();
    }
}