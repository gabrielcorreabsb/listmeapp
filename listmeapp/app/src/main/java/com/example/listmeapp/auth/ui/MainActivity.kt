package com.example.listmeapp.auth.ui // Pacote da sua MainActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listmeapp.R
import com.example.listmeapp.auth.ui.LoginActivity // Se estiver no mesmo pacote
import com.example.listmeapp.data.api.RetrofitClient // Se for chamar o endpoint do backend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var btnLogout: Button // Variável para o botão

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main) // Seu layout com o botão de logout

        // A lógica de insets continua a mesma
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- LÓGICA DO BOTÃO DE LOGOUT ---
        btnLogout = findViewById(R.id.btnLogout) // Encontra o botão no layout

        btnLogout.setOnClickListener {
            performLogout()
        }
        // --- FIM DA LÓGICA DO BOTÃO DE LOGOUT ---
    }

    private fun performLogout() {
        Log.d("MainActivity", "Logout button clicked")

        // --- Parte 1: Limpar o token local ---
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE) // Use o mesmo nome de SharedPreferences
        val token = sharedPreferences.getString("AUTH_TOKEN", null)

        with(sharedPreferences.edit()) {
            remove("AUTH_TOKEN")
            apply()
        }
        Log.i("Auth", "Token removido das SharedPreferences.")

        // --- Parte 2: Chamar o endpoint do backend (Opcional, mas recomendado) ---
        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bearerToken = "Bearer $token"
                    val response = RetrofitClient.instance.logout(bearerToken)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("LogoutAPI", "Logout no backend bem-sucedido: ${response.body()?.message}")
                            Toast.makeText(this@MainActivity, response.body()?.message ?: "Logout realizado (servidor)", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("LogoutAPI", "Falha no logout do backend: ${response.code()} - ${response.errorBody()?.string()}")
                            Toast.makeText(this@MainActivity, "Falha ao registrar logout no servidor", Toast.LENGTH_SHORT).show()
                        }
                        redirectToLogin()
                    }
                } catch (e: Exception) {
                    Log.e("LogoutAPI", "Exceção ao chamar API de logout: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Erro de rede no logout", Toast.LENGTH_SHORT).show()
                        redirectToLogin()
                    }
                }
            }
        } else {
            Log.w("Logout", "Nenhum token local encontrado para logout no backend.")
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        // Certifique-se de que o import da LoginActivity está correto no topo do arquivo
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finaliza MainActivity
    }
}