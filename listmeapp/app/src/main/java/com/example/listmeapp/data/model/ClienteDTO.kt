package com.example.listmeapp.data.model

data class ClienteDTO(
    var id: Long? = null, // Nulo para criação
    var nome: String,
    var telefone: String,
    var endereco: String,
    var cnpj: String,
    var email: String

)