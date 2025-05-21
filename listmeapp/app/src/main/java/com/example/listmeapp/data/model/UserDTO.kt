package com.example.listmeapp.data.model

data class UserDTO(
    val idUsuario: Long,
    val login: String,
    val nome: String,
    val email: String,
    val dataCriacao: String, // String para simplicidade, pode ser Instant/Date com TypeAdapter
    val ativo: Boolean,
    val cargo: String
)