package com.example.listmeapp

import android.app.Application
import com.example.listmeapp.data.api.RetrofitClient

class ListMeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar o RetrofitClient com o contexto da aplicação
        RetrofitClient.initialize(applicationContext)
    }
}