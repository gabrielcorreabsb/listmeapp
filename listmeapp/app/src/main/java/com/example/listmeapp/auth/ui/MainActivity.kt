package com.example.listmeapp.auth.ui // Pacote da sua MainActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

import android.widget.ImageButton // Importar ImageButton

class MainActivity : AppCompatActivity() {

    // Remova ou mantenha btnLogout dependendo se ainda existe no layout
    // private lateinit var btnLogout: Button
    private lateinit var ibLogout: ImageButton // Novo ImageButton para o logout na sidebar
    private lateinit var ibUser: ImageButton // Botão para admin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- LÓGICA DO BOTÃO DE LOGOUT ---
        ibLogout = findViewById(R.id.ibLogout) // Encontra o ImageButton de logout
        ibUser = findViewById(R.id.ibUser)
        ibLogout.setOnClickListener {
            performLogout() // A função performLogout() que você já tem
        }
        // --- FIM DA LÓGICA DO BOTÃO DE LOGOUT ---


        // Verificar o cargo do usuário e mostrar/ocultar o botão de admin
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        val userCargo = sharedPreferences.getString("USER_CARGO", null)

        if (userCargo == "ADMIN") { // Certifique-se que "ADMIN" corresponde ao valor salvo
            ibUser.visibility = View.VISIBLE
            ibUser.setOnClickListener {
                val intent = Intent(this, UserListActivity::class.java)
                startActivity(intent)
            }
        } else {
            ibUser.visibility = View.GONE
        }
    }

    // A função performLogout() e redirectToLogin() permanecem as mesmas
    private fun performLogout() {
        Log.d("MainActivity", "Logout button clicked")
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("AUTH_TOKEN", null)

        // Limpar SharedPreferences COMPLETAMENTE ao fazer logout
        with(sharedPreferences.edit()) {
            clear() // Remove todos os dados (token, cargo, etc.)
            apply()
        }
        Log.i("Auth", "SharedPreferences limpas.")

        // Opcional: Chamar API de logout do backend
        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bearerToken = "Bearer $token"
                    // Supondo que o método logout está em authInstance ou userInstance
                    val response = RetrofitClient.instance.logout(bearerToken) // Ajuste se necessário
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("LogoutAPI", "Logout no backend bem-sucedido.")
                        } else {
                            Log.e("LogoutAPI", "Falha no logout do backend: ${response.code()}")
                        }
                        redirectToLogin() // Redireciona independentemente do resultado da API
                    }
                } catch (e: Exception) {
                    Log.e("LogoutAPI", "Exceção ao chamar API de logout: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        redirectToLogin()
                    }
                }
            }
        } else {
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        // Precisa importar LoginActivity
        val intent = Intent(this@MainActivity, com.example.listmeapp.auth.ui.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}