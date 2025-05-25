package com.listme.dto;

import com.listme.model.ItemOrcamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrcamentoDTO {
    private Long id; // Opcional, pode não ser necessário no DTO de requisição

    @NotNull(message = "ID do produto é obrigatório")
    private Long produtoId; // Para referenciar o produto

    private String nomeProduto; // Para exibição, pode ser preenchido no serviço
    private String unidadeMedidaProduto; // Para exibição

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser positiva")
    private Integer quantidade;

    // O preço unitário pode ser pego do produto no momento da criação do orçamento,
    // ou enviado pelo frontend se houver negociação.
    // Para simplificar, o backend pode buscar o preço atual do produto.
    private BigDecimal precoUnitario; // Preço do produto no momento
    private BigDecimal valorTotalItem; // Calculado

    public static ItemOrcamentoDTO fromEntity(ItemOrcamento item) {
        return ItemOrcamentoDTO.builder()
                .id(item.getId())
                .produtoId(item.getProduto().getId())
                .nomeProduto(item.getProduto().getNome()) // Pega da entidade Produto associada
                .unidadeMedidaProduto(item.getProduto().getUnidadeMedida().name()) // ou getAbreviacao()
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitarioMomento())
                .valorTotalItem(item.getValorTotalItem())
                .build();
    }
}