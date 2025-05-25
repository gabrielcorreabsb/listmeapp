package com.example.listmeapp.data.model

import java.math.BigDecimal
// Se o backend envia LocalDateTime como String, não precisa do import aqui,
// mas precisará de conversão manual ou TypeAdapter se quiser usar LocalDateTime no app.
// import java.time.LocalDateTime

data class OrcamentoResponseDTO(
    val id: Long,
    val cliente: ClienteDTO,        // Reutiliza o ClienteDTO que você já tem
    val funcionario: UserDTO,       // Reutiliza o UserDTO que você já tem
    val dataOrcamento: String,      // Receber como String
    val itens: List<ItemOrcamentoResponseDTO>,
    val valorTotal: BigDecimal,
    val formaPagamento: String,
    val observacoes: String?,
    val status: String              // Nome do Enum como String
)