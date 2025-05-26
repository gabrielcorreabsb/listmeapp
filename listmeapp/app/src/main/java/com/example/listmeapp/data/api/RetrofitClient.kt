package com.example.listmeapp.data.api



import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://listmeapp.tech/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Loga o corpo da requisição/resposta
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Adiciona o interceptor de logging
        .build()

    val instance: AuthApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Usa o OkHttpClient customizado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(AuthApi::class.java)
    }
   val userInstance: UserApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Adicione TypeAdapters para LocalDateTime se necessário
            .build()
        retrofit.create(UserApi::class.java)
    }

val productApi: ProductApi by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()) // Adicione TypeAdapters para LocalDateTime se necessário
        .build()
    retrofit.create(ProductApi::class.java)
}

    val clientApi : ClientApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Adicione TypeAdapters para LocalDateTime se necessário
            .build()
        retrofit.create(ClientApi::class.java)
    }

    val orcamentoApi : OrcamentoApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Adicione TypeAdapters para LocalDateTime se necessário
            .build()
        retrofit.create(OrcamentoApi::class.java)
    }
}