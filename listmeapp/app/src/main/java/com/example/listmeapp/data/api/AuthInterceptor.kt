package com.example.listmeapp.data.api

import com.example.listmeapp.auth.data.TokenManager // Corrija o import se necessário
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Não adicionar token para o endpoint de login
        if (originalRequest.url.encodedPath.contains("/api/auth/login")) {
            return chain.proceed(originalRequest)
        }

        val token = tokenManager.getToken()
        val tokenType = tokenManager.getTokenType()

        if (token != null && tokenType != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "$tokenType $token")
                .build()
            return chain.proceed(newRequest)
        }
        // Procede sem token se não houver um (o servidor deve retornar 401 se necessário)
        return chain.proceed(originalRequest)
    }
}