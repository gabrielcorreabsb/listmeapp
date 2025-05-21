package com.example.listmeapp.auth.data

import com.example.listmeapp.common.Resource
import com.example.listmeapp.data.api.AuthApi
import com.example.listmeapp.data.api.RetrofitClient // Import para pegar o service
import com.example.listmeapp.data.model.LoginRequest
import com.example.listmeapp.data.model.LoginResponse
import com.example.listmeapp.data.model.MessageResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AuthRepository { // Se usar DI (Hilt/Koin), injetaria AuthApi aqui

    // Pega a instância da API do RetrofitClient
    // Em um app com DI, isso seria injetado.
    private val authApi: AuthApi = RetrofitClient.getAuthApiService()

    suspend fun login(username: String, passwordValue: String): Resource<LoginResponse?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.login(LoginRequest(login = username, senha = passwordValue))
                if (response.isSuccessful) {
                    // response.body() pode ser nulo, então Resource.Success PRECISA aceitar data nula
                    Resource.Success(response.body()) // Se response.body() for LoginResponse?, está ok
                } else {
                    // ... (lógica de erro)
                    // Exemplo:
                    Resource.Error("Erro", data = null) // data = null para LoginResponse?
                }
            } catch (e: IOException) {
                Resource.Error("Falha na conexão.", data = null)
            } catch (e: Exception) {
                Resource.Error("Ocorreu um erro: ${e.localizedMessage}", data = null)
            }
        }
    }

    // MUDANÇA AQUI: Resource<MessageResponse> -> Resource<MessageResponse?>
    suspend fun logout(): Resource<MessageResponse?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.logout()
                if (response.isSuccessful) {
                    Resource.Success(response.body()) // Se response.body() for MessageResponse?, está ok
                } else {
                    // ... (lógica de erro)
                    Resource.Error("Falha ao fazer logout", data = null)
                }
            } catch (e: IOException) {
                Resource.Error("Falha na conexão.", data = null)
            } catch (e: Exception) {
                Resource.Error("Ocorreu um erro: ${e.localizedMessage}", data = null)
            }
        }
    }
}