package com.example.listmeapp.data.model

import java.math.BigDecimal // Importar BigDecimal

data class ProdutoDTO(
    var id: Long? = null, // Nulo para criação, preenchido para resposta/edição
    var nome: String,
    var descricao: String? = null,
    var preco: BigDecimal,
    var unidadeMedida: String, // Enviamos e recebemos como String (nome do Enum do backend)
    var urlImagem: String? = null,
    var ativo: Boolean? = null // Nulo para criação (backend define true), preenchido para resposta/edição
)