package com.example.listmeapp.auth.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton // Certifique-se que está importado
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listmeapp.R
// Importe UserListActivity do pacote correto onde você a criou
import com.example.listmeapp.auth.ui.UserListActivity // Supondo que UserListActivity está em admin.ui
import com.example.listmeapp.data.api.RetrofitClient
import com.example.listmeapp.auth.ui.ProductListActivity
import com.example.listmeapp.auth.ui.ClientListActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var ibLogout: ImageButton
    private lateinit var ibUser: ImageButton // Para gerenciar usuários (admin)
    private lateinit var ibManageProducts: ImageButton // Para gerenciar produtos (admin/vendedor)
    private lateinit var ibManagerClient: ImageButton // Para gerenciar clientes (admin/vendedor)
    private lateinit var ibOrcamento: ImageButton // Para os orcamentos (admin/vendedor)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialização dos ImageButtons
        ibLogout = findViewById(R.id.ibLogout)
        ibUser = findViewById(R.id.ibUser) // Assumindo que R.id.ibUser é para gerenciar usuários
        ibManageProducts = findViewById(R.id.ibProduct) // Assumindo R.id.ibManageProducts para produtos
        ibManagerClient = findViewById(R.id.ibClient)
        ibOrcamento = findViewById(R.id.ibOrcamento)



        // Configurar OnClickListener para Logout
        ibLogout.setOnClickListener {
            performLogout()
        }

        // Obter o cargo do usuário
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        val userCargo = sharedPreferences.getString("USER_CARGO", null)

        // Configurar botão de Gerenciar Usuários (Admin)
        if (userCargo == "ADMIN") {
            ibUser.visibility = View.VISIBLE
            ibUser.setOnClickListener {
                val intent = Intent(this, UserListActivity::class.java)
                startActivity(intent)
            }
        } else {
            ibUser.visibility = View.GONE
        }

        // Configurar botão de Gerenciar Produtos, Clientes e Orçamentos (Admin ou Vendedor)
        if (userCargo == "ADMIN" || userCargo == "VENDEDOR") {
            ibManageProducts.visibility = View.VISIBLE
            ibManagerClient.visibility = View.VISIBLE
            ibManageProducts.setOnClickListener {
                val intent = Intent(this, ProductListActivity::class.java)
                startActivity(intent)
            }
            ibManagerClient.setOnClickListener {
                val intent = Intent(this, ClientListActivity::class.java)
                startActivity(intent)
            }
                ibOrcamento.setOnClickListener {
                    val intent = Intent(this, BudgetListActivity::class.java)
                    startActivity(intent)
                }
            } else {
                ibManageProducts.visibility = View.GONE
                ibManagerClient.visibility = View.GONE
            }

    } // FIM DO MÉTODO ONCREATE

    // A função performLogout() e redirectToLogin() permanecem as mesmas
    private fun performLogout() {
        Log.d("MainActivity", "Logout button clicked")
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("AUTH_TOKEN", null)

        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        Log.i("Auth", "SharedPreferences limpas.")

        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bearerToken = "Bearer $token"
                    val response = RetrofitClient.instance.logout(bearerToken) // Usando 'instance' para AuthApi
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("LogoutAPI", "Logout no backend bem-sucedido.")
                        } else {
                            Log.e("LogoutAPI", "Falha no logout do backend: ${response.code()}")
                        }
                        redirectToLogin()
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
        val intent = Intent(this@MainActivity, LoginActivity::class.java) // Importe LoginActivity se necessário
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}