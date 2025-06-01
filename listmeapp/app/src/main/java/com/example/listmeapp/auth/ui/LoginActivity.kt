package com.example.listmeapp.auth.ui // Ou o pacote correto onde está sua UI de login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

// Imports específicos do seu projeto com o pacote base com.example.listmeapp
import com.example.listmeapp.R // << IMPORTANTE: R do seu projeto
import com.example.listmeapp.data.model.LoginRequest
import com.example.listmeapp.data.model.MessageResponse
import com.example.listmeapp.data.api.RetrofitClient
import com.google.android.material.textview.MaterialTextView

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etLogin: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvForgotPassword: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Usa o R.layout do seu projeto


        // Inicializar SharedPreferences para verificar se já existe token
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE) // Nome do arquivo de prefs pode ser app-specific
        val token = sharedPreferences.getString("AUTH_TOKEN", null)

        if (token != null) {
            Log.d("LoginActivity", "Token encontrado ($token), navegando para MainScreen.")
            navigateToMainScreen()
            return // Importante para não continuar a execução do onCreate
        }
        Log.d("LoginActivity", "Nenhum token encontrado, mostrando tela de login.")

        etLogin = findViewById(R.id.etLogin) // Usa o R.id do seu projeto
        etPassword = findViewById(R.id.etPassword) // Usa o R.id do seu projeto
        btnLogin = findViewById(R.id.btnLogin) // Usa o R.id do seu projeto
        progressBar = findViewById(R.id.progressBar) // Usa o R.id do seu projeto
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val loginInput = etLogin.text.toString().trim()
            val passwordInput = etPassword.text.toString().trim()

            if (loginInput.isEmpty()) {
                etLogin.error = "Login é obrigatório"
                Toast.makeText(this, "Login é obrigatório", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (passwordInput.isEmpty()) {
                etPassword.error = "Senha é obrigatória"
                Toast.makeText(this, "Senha é obrigatória", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Limpar erros anteriores, se houver
            etLogin.error = null
            etPassword.error = null

            performLogin(LoginRequest(loginInput, passwordInput))
        }
    }

    private fun performLogin(loginRequest: LoginRequest) {
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false
        etLogin.isEnabled = false
        etPassword.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Certifique-se que RetrofitClient.instance usa o BASE_URL correto (10.0.2.2 para emulador)
                val response = RetrofitClient.instance.login(loginRequest)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    etLogin.isEnabled = true
                    etPassword.isEnabled = true

                    if (response.isSuccessful) {
                        val loginResponseData = response.body()
                        if (loginResponseData != null) {
                            Toast.makeText(this@LoginActivity, "Login bem-sucedido!", Toast.LENGTH_LONG).show()


                            // Salvar o token E O CARGO
                            saveAuthInfo(loginResponseData.token, loginResponseData.user.cargo, loginResponseData.user.nome, loginResponseData.user.email)

                            navigateToMainScreen()

                        } else {
                            Toast.makeText(this@LoginActivity, "Resposta vazia do servidor", Toast.LENGTH_SHORT).show()
                            Log.w("LOGIN_WARNING", "Resposta do servidor bem-sucedida mas corpo vazio.")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        var errorMessage = "Erro ${response.code()}"
                        if (!errorBody.isNullOrEmpty()) {
                            try {
                                val errorResponse = Gson().fromJson(errorBody, MessageResponse::class.java)
                                errorMessage = errorResponse.message
                                Log.e("LOGIN_ERROR_API", "API Error ${response.code()}: $errorMessage (Body: $errorBody)")
                            } catch (e: Exception) {
                                Log.e("LOGIN_ERROR_PARSE", "Erro ao parsear corpo do erro: $errorBody", e)
                                errorMessage = "Erro ${response.code()}: Falha ao processar resposta do servidor."
                            }
                        } else {
                            Log.e("LOGIN_ERROR_API", "API Error ${response.code()} com corpo de erro vazio ou nulo.")
                            errorMessage = "Credenciais inválidas ou erro no servidor (${response.code()})."
                        }
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) { // Captura exceções de rede (ConnectException, UnknownHostException, etc.)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    etLogin.isEnabled = true
                    etPassword.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Falha na conexão: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e("LOGIN_EXCEPTION", "Erro de rede/conexão: ", e)
                }
            }
        }
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("AUTH_TOKEN", token)
            apply()
        }
        Log.i("Auth", "Token salvo nas SharedPreferences.")
    }

    private fun navigateToMainScreen() {
        // Certifique-se que MainActivity existe e está declarada no AndroidManifest.xml
        // e que o import para MainActivity está correto no topo deste arquivo.
        val intent = Intent(this, MainActivity::class.java) // MainActivity deve estar importada corretamente
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun saveAuthInfo(token: String, cargo: String, nome: String, email: String) { // Recebe o cargo como String
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("AUTH_TOKEN", token)
            putString("USER_CARGO", cargo)
            putString("USER_NAME", nome)     // SALVANDO O NOME
            putString("USER_EMAIL", email)   // SALVANDO O EMAIL
            apply()
        }
        Log.i("Auth", "Token e Cargo salvos nas SharedPreferences.")
    }
}
