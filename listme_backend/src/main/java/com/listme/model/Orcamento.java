package com.listme.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orcamentos")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY) // Usar LAZY para não carregar sempre os detalhes do cliente
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente; // Referência ao cliente

    @NotNull(message = "O funcionário (usuário) é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario funcionario; // Referência ao funcionário que gerou

    @Column(name = "data_orcamento", nullable = false, updatable = false)
    private LocalDateTime dataOrcamento;

    @NotEmpty(message = "O orçamento deve ter pelo menos um item")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // EAGER para carregar itens com o orçamento
    @JoinColumn(name = "orcamento_id") // Coluna na tabela ItemOrcamento
    private List<ItemOrcamento> itens = new ArrayList<>();

    @NotNull(message = "O valor total é obrigatório")
    @Positive(message = "O valor total deve ser positivo")
    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @NotBlank(message = "A forma de pagamento é obrigatória")
    @Column(name = "forma_pagamento", nullable = false)
    private String formaPagamento; // Ex: "Cartão de Crédito", "Boleto", "PIX"

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    // Pode adicionar um status: PENDENTE, ENVIADO, APROVADO, REJEITADO, CONCLUIDO
    @Enumerated(EnumType.STRING)
    @Column(name = "status_orcamento")
    private StatusOrcamento status;


    @PrePersist
    protected void onCreate() {
        if (dataOrcamento == null) {
            dataOrcamento = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusOrcamento.PENDENTE; // Status inicial
        }
    }

    @PreUpdate
    protected void onUpdate() {
        calcularValorTotal(); // Recalcula o total ao atualizar
    }

    public void calcularValorTotal() {
        this.valorTotal = itens.stream()
                .map(ItemOrcamento::getValorTotalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void adicionarItem(ItemOrcamento item) {
        this.itens.add(item);
        item.setOrcamento(this); // Se ItemOrcamento tiver uma referência bidirecional
        calcularValorTotal();
    }

    public void removerItem(ItemOrcamento item) {
        this.itens.remove(item);
        item.setOrcamento(null); // Se ItemOrcamento tiver uma referência bidirecional
        calcularValorTotal();
    }

    public enum StatusOrcamento {
        PENDENTE, ENVIADO, APROVADO, REJEITADO, CONCLUIDO, CANCELADO
    }
}