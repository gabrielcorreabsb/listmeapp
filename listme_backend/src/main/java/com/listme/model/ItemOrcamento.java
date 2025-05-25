package com.listme.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Entity
@Builder
@NoArgsConstructor
@Table(name = "itens_orcamento")
public class ItemOrcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore // Para evitar referência cíclica se Orcamento tiver fetch EAGER para itens
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orcamento_id") // Removido nullable = false para permitir criação antes de associar
    private Orcamento orcamento; // Referência ao orçamento pai

    @NotNull(message = "O produto é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER) // Carregar detalhes do produto com o item
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser positiva")
    @Column(nullable = false)
    private Integer quantidade;

    @NotNull(message = "O preço unitário no momento do orçamento é obrigatório")
    @Positive(message = "O preço unitário deve ser positivo")
    @Column(name = "preco_unitario_momento", nullable = false)
    private BigDecimal precoUnitarioMomento;

    @NotNull(message = "O valor total do item é obrigatório")
    @Positive(message = "O valor total do item deve ser positivo")
    @Column(name = "valor_total_item", nullable = false)
    private BigDecimal valorTotalItem;

    // Construtor para garantir que os camposNotNull não sejam nulos ao criar
    @Builder
    public ItemOrcamento(Long id, Orcamento orcamento, Produto produto,
                         @NotNull Integer quantidade, @NotNull BigDecimal precoUnitarioMomento,
                         BigDecimal valorTotalItem /* pode ser nulo aqui, @PrePersist calcula */) {
        this.id = id;
        this.orcamento = orcamento;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitarioMomento = precoUnitarioMomento;
        // this.valorTotalItem é calculado no @PrePersist
        // Mas para evitar NPE na soma, é melhor que o getValorTotalItem retorne ZERO se ainda não calculado
    }


    @PrePersist
    @PreUpdate
    private void calcularTotalItem() {
        if (this.quantidade != null && this.precoUnitarioMomento != null &&
                this.quantidade > 0 && this.precoUnitarioMomento.compareTo(BigDecimal.ZERO) > 0) { // Condições para valor positivo
            this.valorTotalItem = this.precoUnitarioMomento.multiply(new BigDecimal(this.quantidade));
        } else {
            this.valorTotalItem = BigDecimal.ZERO; // Default para zero se condições não atendidas
        }
    }

    // Adicione um getter que retorne BigDecimal.ZERO se valorTotalItem for null

    public BigDecimal getValorTotalItem() {
        return valorTotalItem == null ? BigDecimal.ZERO : valorTotalItem;
    }
}