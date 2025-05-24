package com.example.listmeapp.data.model

data class UsuarioCreateRequest(
    val nome: String,
    val login: String,
    val email: String,
    val senha: String,
    val cargo: String // Enviamos como String (ex: "ADMIN", "VENDEDOR")
)