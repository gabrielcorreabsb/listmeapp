package com.example.listmeapp.data.api

import com.example.listmeapp.data.model.LoginRequest
import com.example.listmeapp.data.model.LoginResponse
import com.example.listmeapp.data.model.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login") // Seu endpoint de login
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/logout") // Seu endpoint de logout
    suspend fun logout(): Response<MessageResponse> // Supondo que o logout também precise do token
}