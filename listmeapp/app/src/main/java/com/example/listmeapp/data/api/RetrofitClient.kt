package com.example.listmeapp.data.api

import android.content.Context
import com.example.listmeapp.auth.data.TokenManager // Corrija o import
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // MUITO IMPORTANTE:
    // Para emulador Android, use "http://10.0.2.2:PORTA_DO_SEU_BACKEND/"
    // Se for dispositivo físico na mesma rede Wi-Fi, use o IP da sua máquina: "http://SEU_IP_NA_REDE:PORTA/"
    private const val BASE_URL = "http://10.0.2.2:8080/" // Ajuste a porta se necessário

    private lateinit var retrofitInstance: Retrofit
    private lateinit var internalAuthApi: AuthApi
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        val appContext = context.applicationContext // Usar applicationContext

        val tokenManager = TokenManager(appContext)
        val authInterceptor = AuthInterceptor(tokenManager)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Para ver logs de request/response
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofitInstance = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Para converter JSON
            .build()

        internalAuthApi = retrofitInstance.create(AuthApi::class.java)
        isInitialized = true
    }

    fun getAuthApiService(): AuthApi {
        if (!isInitialized) {
            throw IllegalStateException("RetrofitClient deve ser inicializado primeiro em MainApplication.")
        }
        return internalAuthApi
    }
}