package com.example.listmeapp.auth.ui // Ou o pacote da sua Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.listmeapp.R
import com.example.listmeapp.data.api.RetrofitClient
import com.example.listmeapp.data.model.Cargo
import com.example.listmeapp.data.model.MessageResponse
import com.example.listmeapp.data.model.UsuarioCreateRequest
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: TextInputEditText
    private lateinit var etRegisterLogin: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etRegisterPassword: TextInputEditText
    private lateinit var spinnerCargo: Spinner
    private lateinit var btnRegister: Button
    private lateinit var pbRegister: ProgressBar
    private lateinit var tvLoginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etFullName = findViewById(R.id.etFullName)
        etRegisterLogin = findViewById(R.id.etRegisterLogin)
        etEmail = findViewById(R.id.etEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        spinnerCargo = findViewById(R.id.spinnerCargo)
        btnRegister = findViewById(R.id.btnRegister)
        pbRegister = findViewById(R.id.pbRegister)

        // Configurar o Spinner com os valores do Enum Cargo
        // Obtém os nomes dos enums, traduz se necessário (aqui apenas nomes)
        val cargoNames = Cargo.values().map { it.name.replace("_", " ").capitalizeWords() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cargoNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCargo.adapter = adapter

        btnRegister.setOnClickListener {
            performRegistration()
        }

        tvLoginLink.setOnClickListener {
            // Navegar de volta para LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    // Função helper para capitalizar palavras (ex: FUNCIONARIO -> Funcionario)
    fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }


    private fun performRegistration() {
        val fullName = etFullName.text.toString().trim()
        val login = etRegisterLogin.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etRegisterPassword.text.toString().trim()
        val selectedCargoNameFromSpinner = spinnerCargo.selectedItem.toString().replace(" ", "_").uppercase() // Ex: "Funcionario" -> "FUNCIONARIO"

        // Validação básica
        if (fullName.isEmpty() || login.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Todos os campos são obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email inválido"
            Toast.makeText(this, "Formato de email inválido", Toast.LENGTH_SHORT).show()
            return
        }
        // Outras validações (ex: tamanho da senha) podem ser adicionadas

        pbRegister.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        val request = UsuarioCreateRequest(
            nome = fullName,
            login = login,
            email = email,
            senha = password,
            cargo = selectedCargoNameFromSpinner
        )

// Obter o token do admin logado
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("AUTH_TOKEN", null)

        if (authToken == null) {
            Toast.makeText(this, "Erro: Administrador não autenticado.", Toast.LENGTH_LONG).show()
            pbRegister.visibility = View.GONE
            btnRegister.isEnabled = true
            // Idealmente, redirecionar para login ou tratar o erro de forma mais robusta
            return
        }

        val bearerToken = "Bearer $authToken" // Adicionar o prefixo "Bearer "

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.userInstance.createUser(bearerToken, request) // Passar o token

                withContext(Dispatchers.Main) {
                    pbRegister.visibility = View.GONE
                    btnRegister.isEnabled = true

                    if (response.isSuccessful) {
                        val newUser = response.body()
                        Toast.makeText(this@RegisterActivity, "Usuário ${newUser?.nome} cadastrado com sucesso!", Toast.LENGTH_LONG).show()
                        Log.d("RegisterSuccess", "Usuário criado: $newUser")
                        // Navegar para Login ou fechar RegisterActivity
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        var errorMessage = "Erro ${response.code()}"
                        if (!errorBody.isNullOrEmpty()) {
                            try {
                                // Tenta parsear como uma mensagem de erro simples se o backend enviar { "message": "..." }
                                // ou diretamente a string se for o caso.
                                val errorResponse = Gson().fromJson(errorBody, MessageResponse::class.java)
                                errorMessage = errorResponse.message // Se o backend envia { "message": "..."}
                            } catch (e: Exception) {
                                errorMessage = errorBody // Se o backend envia a string diretamente
                                Log.e("RegisterErrorParse", "Erro ao parsear corpo do erro: $errorBody", e)
                            }
                        }
                        Toast.makeText(this@RegisterActivity, "Falha no cadastro: $errorMessage", Toast.LENGTH_LONG).show()
                        Log.e("RegisterErrorAPI", "Código: ${response.code()}, Mensagem: $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbRegister.visibility = View.GONE
                    btnRegister.isEnabled = true
                    Toast.makeText(this@RegisterActivity, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("RegisterException", "Erro: ${e.message}", e)
                }
            }
        }
    }
}