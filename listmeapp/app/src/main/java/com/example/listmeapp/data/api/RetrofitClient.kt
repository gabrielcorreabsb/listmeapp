package com.example.listmeapp.data.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // ATENÇÃO: Se estiver testando com emulador Android, localhost do seu PC é 10.0.2.2
    // Se estiver testando com dispositivo físico na mesma rede Wi-Fi, use o IP local do seu PC (ex: 192.168.1.10)
    private const val BASE_URL = "http://10.0.2.2:8080/" // MUDE AQUI SE NECESSÁRIO

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
}