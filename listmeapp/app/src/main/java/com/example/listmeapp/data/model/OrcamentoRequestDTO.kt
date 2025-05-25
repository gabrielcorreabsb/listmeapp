package com.example.listmeapp.data.model

data class OrcamentoRequestDTO(
    val clienteId: Long,
    val itens: List<ItemOrcamentoRequestDTO>,
    val formaPagamento: String,
    val observacoes: String?,
    val status: String? // Enviar o nome do Enum (ex: "PENDENTE")
)