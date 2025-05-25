package com.example.listmeapp.data.api;

import com.example.listmeapp.data.model.ProdutoDTO;


import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface ProductApi {

    @POST("api/produtos")
    suspend fun createProduct(
            @Header("Authorization") token: String,
            @Body productDTO: ProdutoDTO
    ): Response<ProdutoDTO>

    @GET("api/produtos") // Ou /api/produtos/ativos se quiser apenas os ativos
    suspend fun getProducts(
            @Header("Authorization") token: String
    ): Response<List<ProdutoDTO>>

    @GET("api/produtos/{id}")
    suspend fun getProductById(
            @Header("Authorization") token: String,
            @Path("id") productId: Long
    ): Response<ProdutoDTO>

    @PUT("api/produtos/{id}")
    suspend fun updateProduct(
            @Header("Authorization")token: String,
            @Path("id")productId: Long,
            @Body productDTO: ProdutoDTO
    ): Response<ProdutoDTO>

    @DELETE("api/produtos/{id}")
    suspend fun deleteProduct(
            @Header("Authorization") token: String,
            @Path("id") productId: Long
    ): Response<Void>

    @GET("api/produtos/buscar")
    suspend fun searchProductsByName(
            @Header("Authorization")token: String,
            @Query("nome")name: String
    ): Response<List<ProdutoDTO>>
}