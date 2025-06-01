package com.example.listmeapp.data.api

import com.example.listmeapp.data.model.LoginRequest
import com.example.listmeapp.data.model.LoginResponse
import com.example.listmeapp.data.model.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("api/auth/login") // Seu endpoint de login
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    // NOVO MÉTODO DE LOGOUT
    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<MessageResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Query("email") email: String): Response<MessageResponse>
}
