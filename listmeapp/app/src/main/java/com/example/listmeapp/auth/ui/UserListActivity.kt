package com.example.listmeapp.auth.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.api.UserApi
import com.example.listmeapp.data.api.RetrofitClient
import com.example.listmeapp.data.model.*
import com.example.listmeapp.auth.ui.UserListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserListActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var userListAdapter: UserListAdapter
    private lateinit var pbUserList: ProgressBar
    private lateinit var tvNoUsers: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var fabAddUser: FloatingActionButton
    private var authToken: String? = null

    private val cargoValues = Cargo.values() // Enum Cargo do seu modelo
    private val cargoDisplayNames = cargoValues.map { it.name.replace("_", " ").capitalizeWords() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        toolbar = findViewById(R.id.toolbarUserList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        rvUsers = findViewById(R.id.rvUsers)
        pbUserList = findViewById(R.id.pbUserList)
        tvNoUsers = findViewById(R.id.tvNoUsers)
        fabAddUser = findViewById(R.id.fabAddUser)

        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("AUTH_TOKEN", null)

        if (authToken == null) {
            Toast.makeText(this, "Erro de autenticação. Faça login como admin.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fabAddUser.setOnClickListener {
            showUserFormDialog(null) // null para indicar que é um novo usuário
        }

        setupRecyclerView()
        fetchUsers()
    }

    // Helper para capitalizar palavras
    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }

    private fun setupRecyclerView() {
        userListAdapter = UserListAdapter(emptyList(),
            onEditClick = { user ->
                showUserFormDialog(user)
            },
            onDeleteClick = { user ->
                showDeleteConfirmationDialog(user)
            }
        )
        rvUsers.adapter = userListAdapter
    }

    private fun fetchUsers() {
        pbUserList.visibility = View.VISIBLE
        tvNoUsers.visibility = View.GONE
        rvUsers.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.userInstance.getUsers(bearerToken)
                withContext(Dispatchers.Main) {
                    pbUserList.visibility = View.GONE
                    if (response.isSuccessful) {
                        val users = response.body()
                        if (users.isNullOrEmpty()) {
                            tvNoUsers.visibility = View.VISIBLE
                            rvUsers.visibility = View.GONE
                        } else {
                            userListAdapter.updateUsers(users)
                            tvNoUsers.visibility = View.GONE
                            rvUsers.visibility = View.VISIBLE
                        }
                    } else {
                        tvNoUsers.visibility = View.VISIBLE
                        Toast.makeText(this@UserListActivity, "Erro ao buscar usuários: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbUserList.visibility = View.GONE
                    tvNoUsers.visibility = View.VISIBLE
                    Toast.makeText(this@UserListActivity, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showUserFormDialog(userToEdit: UsuarioResponse?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_form, null)
        val etFullName = dialogView.findViewById<TextInputEditText>(R.id.etDialogFullName)
        val etLogin = dialogView.findViewById<TextInputEditText>(R.id.etDialogLogin)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etDialogEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etDialogPassword)
        val tvPasswordHint = dialogView.findViewById<TextView>(R.id.tvPasswordHint)
        val spinnerCargo = dialogView.findViewById<Spinner>(R.id.spinnerDialogCargo)
        val switchAtivo = dialogView.findViewById<SwitchMaterial>(R.id.switchDialogAtivo)
        val pbDialog = dialogView.findViewById<ProgressBar>(R.id.pbDialogUserForm)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cargoDisplayNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCargo.adapter = adapter

        val dialogTitle: String
        if (userToEdit != null) {
            dialogTitle = "Editar Usuário"
            etFullName.setText(userToEdit.nome)
            etLogin.setText(userToEdit.login)
            etEmail.setText(userToEdit.email)
            etPassword.hint = "Nova Senha (deixe em branco para não alterar)"
            tvPasswordHint.visibility = View.VISIBLE
            switchAtivo.isChecked = userToEdit.ativo
            val cargoPosition = cargoValues.indexOfFirst { it.name == userToEdit.cargo }
            if (cargoPosition != -1) spinnerCargo.setSelection(cargoPosition)
        } else {
            dialogTitle = "Adicionar Novo Usuário"
            switchAtivo.isChecked = true
            tvPasswordHint.visibility = View.GONE
            etPassword.hint = "Senha" // Senha obrigatória para novo
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(if (userToEdit != null) "Salvar" else "Adicionar", null)
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val fullName = etFullName.text.toString().trim()
                val login = etLogin.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val selectedCargoEnumName = cargoValues[spinnerCargo.selectedItemPosition].name
                val ativo = switchAtivo.isChecked

                // Validações
                if (fullName.isEmpty() || login.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Nome, Login e Email são obrigatórios.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Email inválido"
                    return@setOnClickListener
                } else {
                    etEmail.error = null
                }

                if (userToEdit == null && password.isEmpty()) {
                    etPassword.error = "Senha obrigatória para novo usuário."
                    return@setOnClickListener
                }

                // Validação do formato/tamanho da senha SE ela foi preenchida (tanto para novo quanto para edição)
                if (password.isNotEmpty()) {
                    if (password.length < 6) {
                        etPassword.error = "Senha: Mínimo 6 caracteres."
                        return@setOnClickListener
                    }
                    // Adicione sua regex de complexidade de senha aqui se desejar
                    // val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!])(.{6,})$".toRegex()
                    // if (!password.matches(passwordPattern)) {
                    //     etPassword.error = "Senha deve conter letra, número e caractere especial."
                    //     return@setOnClickListener
                    // }
                }
                etPassword.error = null


                pbDialog.visibility = View.VISIBLE
                positiveButton.isEnabled = false // Desabilita botão durante a chamada

                if (userToEdit != null) { // EDIÇÃO
                    val updateRequest = UsuarioUpdateRequest(
                        nome = fullName, login = login, email = email,
                        cargo = selectedCargoEnumName, ativo = ativo
                    )
                    // Adicionar lógica para enviar senha apenas se password não estiver em branco
                    // e seu backend souber lidar com isso no UsuarioUpdateRequest
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val bearerToken = "Bearer $authToken"
                            val response = RetrofitClient.userInstance.updateUser(bearerToken, userToEdit.idUsuario, updateRequest)
                            withContext(Dispatchers.Main) {
                                pbDialog.visibility = View.GONE
                                positiveButton.isEnabled = true
                                if (response.isSuccessful) {
                                    Toast.makeText(this@UserListActivity, "Usuário atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                                    fetchUsers()
                                    dialog.dismiss()
                                } else {
                                    val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                                    showApiError(errorMsg, "Falha ao atualizar")
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                pbDialog.visibility = View.GONE
                                positiveButton.isEnabled = true
                                Toast.makeText(this@UserListActivity, "Exceção ao atualizar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else { // CRIAÇÃO
                    val createRequest = UsuarioCreateRequest(
                        nome = fullName, login = login, email = email,
                        senha = password, cargo = selectedCargoEnumName
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val bearerToken = "Bearer $authToken"
                            val response = RetrofitClient.userInstance.createUser(bearerToken, createRequest)
                            withContext(Dispatchers.Main) {
                                pbDialog.visibility = View.GONE
                                positiveButton.isEnabled = true
                                if (response.isSuccessful) {
                                    Toast.makeText(this@UserListActivity, "Usuário '${response.body()?.nome}' criado!", Toast.LENGTH_SHORT).show()
                                    fetchUsers()
                                    dialog.dismiss()
                                } else {
                                    val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                                    showApiError(errorMsg, "Falha ao criar")
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                pbDialog.visibility = View.GONE
                                positiveButton.isEnabled = true
                                Toast.makeText(this@UserListActivity, "Exceção ao criar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showApiError(errorBody: String?, defaultTitlePrefix: String) {
        var errorMessage = defaultTitlePrefix
        if (!errorBody.isNullOrEmpty()) {
            errorMessage = try {
                val errorResponse = Gson().fromJson(errorBody, MessageResponse::class.java)
                "$defaultTitlePrefix: ${errorResponse.message}"
            } catch (parseEx: Exception) {
                "$defaultTitlePrefix: $errorBody"
            }
        }
        Toast.makeText(this@UserListActivity, errorMessage, Toast.LENGTH_LONG).show()
    }


    private fun showDeleteConfirmationDialog(user: UsuarioResponse) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir o usuário ${user.nome} (ID: ${user.idUsuario})?")
            .setPositiveButton("Excluir") { _, _ ->
                deleteUser(user.idUsuario)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(userId: Int) {
        pbUserList.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.userInstance.deleteUser(bearerToken, userId)
                withContext(Dispatchers.Main) {
                    pbUserList.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserListActivity, "Usuário excluído com sucesso!", Toast.LENGTH_SHORT).show()
                        fetchUsers()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                        showApiError(errorMsg, "Falha ao excluir")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbUserList.visibility = View.GONE
                    Toast.makeText(this@UserListActivity, "Exceção ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}