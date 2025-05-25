package com.listme.dto;

import com.listme.model.Orcamento.StatusOrcamento; // Importe seu enum
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoRequestDTO {
    @NotNull(message = "ID do cliente é obrigatório")
    private Long clienteId;

    // O ID do funcionário será pego do usuário autenticado no backend
    // private Long funcionarioId; // Não precisa enviar do frontend

    @NotEmpty(message = "O orçamento deve ter pelo menos um item")
    @Valid // Para validar os ItemOrcamentoDTO aninhados
    private List<ItemOrcamentoDTO> itens;

    @NotBlank(message = "A forma de pagamento é obrigatória")
    private String formaPagamento;

    private String observacoes;

    // Status pode ser definido pelo backend ou opcionalmente enviado
    private StatusOrcamento status;
}