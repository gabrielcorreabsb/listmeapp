package com.example.listmeapp.data.model

data class UsuarioUpdateRequest(
    val nome: String,
    val login: String,
    val email: String,
    val cargo: String,
    val ativo: Boolean,
    val senha: String? = null
)