package com.example.listmeapp.data.model

import java.math.BigDecimal

// DTO para enviar na requisição de criação/atualização de orçamento
data class ItemOrcamentoRequestDTO(
    val produtoId: Long,
    val quantidade: Int
    // O preço unitário será buscado pelo backend no momento da criação
)

// DTO para receber na resposta do orçamento (pode ser o mesmo se os campos forem iguais)
data class ItemOrcamentoResponseDTO(
    val id: Long?,
    val produtoId: Long,
    val nomeProduto: String?,
    val unidadeMedidaProduto: String?,
    val quantidade: Int,
    val precoUnitario: BigDecimal?, // No backend é precoUnitarioMomento
    val valorTotalItem: BigDecimal?
)