package com.example.listmeapp.data.api

import com.example.listmeapp.data.model.ClienteDTO
import retrofit2.Response
import retrofit2.http.*

interface ClientApi {

    @POST("api/clientes")
    suspend fun createClient(
        @Header("Authorization") token: String,
        @Body clientDTO: ClienteDTO
    ): Response<ClienteDTO>

    @GET("api/clientes")
    suspend fun getClients(
        @Header("Authorization") token: String
    ): Response<List<ClienteDTO>>

    @GET("api/clientes/{id}")
    suspend fun getClientById(
        @Header("Authorization") token: String,
        @Path("id") clientId: Long
    ): Response<ClienteDTO>

    @PUT("api/clientes/{id}")
    suspend fun updateClient(
        @Header("Authorization") token: String,
        @Path("id") clientId: Long,
        @Body clientDTO: ClienteDTO
    ): Response<ClienteDTO>

    @DELETE("api/clientes/{id}")
    suspend fun deleteClient(
        @Header("Authorization") token: String,
        @Path("id") clientId: Long
    ): Response<Void>
}