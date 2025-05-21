package com.example.listmeapp.data.model

data class LoginResponse(
    val token: String,
    val type: String, // ex: "Bearer"
    val user: UserDTO
)