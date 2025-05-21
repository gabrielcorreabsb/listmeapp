package com.example.listmeapp.auth.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        // Você pode adicionar chaves para UserDTO se quiser salvar informações do usuário também
    }

    fun saveToken(token: String, type: String) {
        prefs.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_TOKEN_TYPE, type)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun getTokenType(): String? {
        return prefs.getString(KEY_TOKEN_TYPE, null)
    }

    fun clearToken() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_TOKEN_TYPE)
            .apply()
    }

    fun isUserLoggedIn(): Boolean {
        return getToken() != null
    }
}