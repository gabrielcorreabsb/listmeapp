package com.example.listmeapp.auth.ui // Ou o pacote correto da sua Activity

import android.content.Context
// import android.content.Intent // Não usado diretamente nesta Activity se RegisterActivity foi removida
import android.os.Bundle
import android.util.Log
import android.util.Patterns // Para validação de Email
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.api.RetrofitClient
import com.example.listmeapp.data.model.* // Importar Cargo, UsuarioCreateRequest, UsuarioUpdateRequest, UsuarioResponse, MessageResponse
// import com.example.listmeapp.auth.ui.UserListAdapter // Se UserListAdapter estiver neste pacote
import com.example.listmeapp.auth.ui.UserListAdapter // Exemplo se estiver em ui.adapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class UserListActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var userListAdapter: UserListAdapter
    private lateinit var pbUserList: ProgressBar
    private lateinit var tvNoUsers: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var fabAddUser: FloatingActionButton
    private var authToken: String? = null

    private val cargoValues = Cargo.values()
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
        // A visibilidade do FAB e das opções de edição/deleção já são controladas
        // pelo fato de que apenas Admins chegam a esta tela.

        if (authToken == null) {
            Toast.makeText(this, "Erro de autenticação. Faça login como admin.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fabAddUser.setOnClickListener {
            showUserFormDialog(null)
        }




        setupRecyclerView()
        fetchUsers()
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }

    private fun setupRecyclerView() {
        userListAdapter = UserListAdapter(emptyList(),
            onEditClick = { user ->
                showUserFormDialog(user)
            },
            onDeleteClick = { user ->
                showDeleteConfirmationDialog(user)
            }
            // isAdmin não é mais necessário passar para o adapter se esta tela SÓ é acessada por admin
            // e todas as ações são permitidas para admin.
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
                val response = RetrofitClient.userInstance.getUsers(bearerToken) // Corrigido
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
                        Log.e("FetchUsersError", "Código: ${response.code()}, Mensagem: ${response.errorBody()?.string()}")
                        Toast.makeText(this@UserListActivity, "Erro ao buscar usuários: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbUserList.visibility = View.GONE
                    tvNoUsers.visibility = View.VISIBLE
                    Log.e("FetchUsersException", "Erro: ${e.message}", e)
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
            val cargoPosition = cargoValues.indexOfFirst { it.name.equals(userToEdit.cargo, ignoreCase = true) }
            if (cargoPosition != -1) spinnerCargo.setSelection(cargoPosition)
        } else {
            dialogTitle = "Adicionar Novo Usuário"
            switchAtivo.isChecked = true
            tvPasswordHint.visibility = View.GONE
            etPassword.hint = "Senha"
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
                val passwordInput = etPassword.text.toString() // Não fazer trim aqui ainda para validar o pattern
                val selectedCargoEnumName = cargoValues[spinnerCargo.selectedItemPosition].name
                val ativo = switchAtivo.isChecked

                var isValid = true

                // Validação Nome
                val nomePattern = "^[a-zA-ZÀ-ÿ\\s]+$".toRegex()
                if (fullName.isEmpty()) {
                    etFullName.error = "Nome é obrigatório"
                    isValid = false
                } else if (fullName.length < 3 || fullName.length > 100) {
                    etFullName.error = "Nome deve ter entre 3 e 100 caracteres"
                    isValid = false
                } else if (!fullName.matches(nomePattern)) {
                    etFullName.error = "Nome deve conter apenas letras e espaços"
                    isValid = false
                } else {
                    etFullName.error = null
                }

                // Validação Login
                val loginPattern = "^[a-zA-Z0-9._-]+$".toRegex()
                if (login.isEmpty()) {
                    etLogin.error = "Login é obrigatório"
                    isValid = false
                } else if (login.length < 3 || login.length > 50) {
                    etLogin.error = "Login deve ter entre 3 e 50 caracteres"
                    isValid = false
                } else if (!login.matches(loginPattern)) {
                    etLogin.error = "Login: letras, números, '.', '_' ou '-'"
                    isValid = false
                } else {
                    etLogin.error = null
                }

                // Validação Email
                if (email.isEmpty()) {
                    etEmail.error = "Email é obrigatório"
                    isValid = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Formato de email inválido"
                    isValid = false
                } else {
                    etEmail.error = null
                }

                // Validação Senha
                if (userToEdit == null && passwordInput.isEmpty()) { // Obrigatória para novo usuário
                    etPassword.error = "Senha obrigatória para novo usuário"
                    isValid = false
                } else if (passwordInput.isNotEmpty()) { // Validar se preenchida (para novo ou edição)
                    if (passwordInput.length < 6) {
                        etPassword.error = "Senha: mínimo 6 caracteres"
                        isValid = false
                    } else {
                        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!])(.{6,})$".toRegex()
                        if (!passwordInput.matches(passwordPattern)) {
                            etPassword.error = "Senha: letra, número e caractere especial"
                            isValid = false
                        } else {
                            etPassword.error = null
                        }
                    }
                } else { // Senha vazia na edição é OK, limpa o erro
                    etPassword.error = null
                }


                if (!isValid) {
                    Toast.makeText(this@UserListActivity, "Corrija os campos destacados.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                pbDialog.visibility = View.VISIBLE
                positiveButton.isEnabled = false

                if (userToEdit != null) { // EDIÇÃO
                    val updateRequest = UsuarioUpdateRequest(
                        nome = fullName,
                        login = login,
                        email = email,
                        cargo = selectedCargoEnumName,
                        ativo = ativo,
                        senha = if (passwordInput.isNotEmpty()) passwordInput else null // Envia senha apenas se preenchida
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val bearerToken = "Bearer $authToken"
                            val response = RetrofitClient.userInstance.updateUser(bearerToken, userToEdit.idUsuario, updateRequest)
                            withContext(Dispatchers.Main) {
                                pbDialog.visibility = View.GONE
                                positiveButton.isEnabled = true
                                if (response.isSuccessful) {
                                    Toast.makeText(this@UserListActivity, "Usuário '${response.body()?.nome}' atualizado!", Toast.LENGTH_SHORT).show()
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
                                Log.e("UpdateUserExc", "Erro: ${e.message}", e)
                                Toast.makeText(this@UserListActivity, "Exceção ao atualizar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else { // CRIAÇÃO
                    val createRequest = UsuarioCreateRequest(
                        nome = fullName,
                        login = login,
                        email = email,
                        senha = passwordInput, // passwordInput já foi validada como não vazia
                        cargo = selectedCargoEnumName
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
                                Log.e("CreateUserExc", "Erro: ${e.message}", e)
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
                "$defaultTitlePrefix: $errorBody (Falha ao parsear erro detalhado)"
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
                val response = RetrofitClient.userInstance.deleteUser(bearerToken, userId) // Corrigido
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
                    Log.e("DeleteUserExc", "Erro: ${e.message}", e)
                    Toast.makeText(this@UserListActivity, "Exceção ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}