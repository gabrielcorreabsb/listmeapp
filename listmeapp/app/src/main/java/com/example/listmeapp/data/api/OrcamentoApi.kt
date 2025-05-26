package com.example.listmeapp.data.api

import com.example.listmeapp.data.model.OrcamentoRequestDTO
import com.example.listmeapp.data.model.OrcamentoResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface OrcamentoApi {

    @POST("api/orcamentos")
    suspend fun createOrcamento(
        @Header("Authorization") token: String,
        @Body orcamentoRequestDTO: OrcamentoRequestDTO
    ): Response<OrcamentoResponseDTO>

    @GET("api/orcamentos")
    suspend fun getOrcamentos(
        @Header("Authorization") token: String
    ): Response<List<OrcamentoResponseDTO>>

    @GET("api/orcamentos/{id}")
    suspend fun getOrcamentoById(
        @Header("Authorization") token: String,
        @Path("id") orcamentoId: Long
    ): Response<OrcamentoResponseDTO>

    @PUT("api/orcamentos/{id}")
    suspend fun updateOrcamento(
        @Header("Authorization") token: String,
        @Path("id") orcamentoId: Long,
        @Body orcamentoRequestDTO: OrcamentoRequestDTO // Usar o mesmo DTO para atualizar por simplicidade
    ): Response<OrcamentoResponseDTO>

    @DELETE("api/orcamentos/{id}")
    suspend fun deleteOrcamento(
        @Header("Authorization") token: String,
        @Path("id") orcamentoId: Long
    ): Response<Void>

    @POST("api/orcamentos/{id}/enviar-email")
    suspend fun sendOrcamentoEmail(
        @Header("Authorization") token: String,
        @Path("id") orcamentoId: Long
    ): Response<Void> // Ou uma resposta com mensagem, se o backend fornecer

    @PUT("api/orcamentos/{id}/status")
    suspend fun updateOrcamentoStatus(
        @Header("Authorization") token: String,
        @Path("id") orcamentoId: Long,
        @Query("status") novoStatus: String // Envia o nome do Enum como String
    ): Response<OrcamentoResponseDTO> // O backend retorna o orçamento atualizado
}