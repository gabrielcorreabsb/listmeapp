package com.example.listmeapp

import android.app.Application
import com.example.listmeapp.data.api.RetrofitClient

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.initialize(this)
    }
}