package com.example.listmeapp.data.api

import com.example.listmeapp.data.model.UsuarioCreateRequest
import com.example.listmeapp.data.model.UsuarioResponse
import com.example.listmeapp.data.model.UsuarioUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    // Criar Usuário
    @POST("api/usuarios")
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body userCreateRequest: UsuarioCreateRequest
    ): Response<UsuarioResponse>

    // Listar Usuários
    @GET("api/usuarios")
    suspend fun getUsers(
        @Header("Authorization") token: String
    ): Response<List<UsuarioResponse>>

    // Editar Usuário
    @PUT("api/usuarios/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body userUpdateRequest: UsuarioUpdateRequest
    ): Response<UsuarioResponse>

    // Deletar Usuário
    @DELETE("api/usuarios/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<Void>
}