package com.example.listmeapp.data.model

import com.google.gson.annotations.SerializedName

data class UsuarioResponse(
    @SerializedName("idUsuario")
    val idUsuario: Int,
    val login: String,
    val nome: String,
    val email: String,
    val dataCriacao: String, // Recebido como String
    val ativo: Boolean,
    val cargo: String // Recebido como String (nome do Enum)
    // Faltam: ultimoAcesso, tentativasLogin, roles (se você quiser usá-los no frontend)
)