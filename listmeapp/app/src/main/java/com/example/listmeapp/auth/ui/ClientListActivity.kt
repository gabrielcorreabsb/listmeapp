package com.example.listmeapp.auth.ui // Ou o pacote correto da sua Activity

import android.app.Activity // Import para Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter // Para o Spinner no diálogo (se usado)
import android.widget.ProgressBar
import android.widget.Spinner     // Para o Spinner no diálogo (se usado)
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.api.RetrofitClient
import com.example.listmeapp.data.model.ClienteDTO
import com.example.listmeapp.data.model.MessageResponse
import com.example.listmeapp.auth.ui.ClientListAdapter // Ajuste o pacote do adapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.santalu.maskara.Mask // Imports da maskara
import com.santalu.maskara.MaskChangedListener
import com.santalu.maskara.MaskStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ClientListActivity : AppCompatActivity() {

    private lateinit var rvClients: RecyclerView
    private lateinit var clientListAdapter: ClientListAdapter
    private lateinit var pbClientList: ProgressBar
    private lateinit var tvNoClients: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var fabAddClient: FloatingActionButton
    private var authToken: String? = null
    private var userCargo: String? = null
    private var isAdmin: Boolean = false
    private var canCreateEdit: Boolean = false

    private var isSelectMode: Boolean = false // Para saber se está em modo de seleção

    // Listeners para as máscaras, para poder obter valor não mascarado se necessário
    private lateinit var cnpjCpfListener: MaskChangedListener
    private lateinit var phoneListener: MaskChangedListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_list)

        toolbar = findViewById(R.id.toolbarClientList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            // Se estiver em modo de seleção e o usuário voltar, considera como cancelado
            if (isSelectMode) {
                setResult(Activity.RESULT_CANCELED)
            }
            onBackPressedDispatcher.onBackPressed()
        }

        rvClients = findViewById(R.id.rvClients)
        pbClientList = findViewById(R.id.pbClientList)
        tvNoClients = findViewById(R.id.tvNoClients)
        fabAddClient = findViewById(R.id.fabAddClient)

        // Verifica se a activity foi chamada em modo de seleção
        isSelectMode = intent.getBooleanExtra("SELECT_MODE", false)

        if (isSelectMode) {
            toolbar.title = "Selecionar Cliente"
            fabAddClient.visibility = View.GONE // Esconde FAB no modo de seleção
        } else {
            toolbar.title = "Gerenciar Clientes" // Título padrão
        }

        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("AUTH_TOKEN", null)
        userCargo = sharedPreferences.getString("USER_CARGO", null)
        isAdmin = userCargo == "ADMIN"
        canCreateEdit = userCargo == "ADMIN" || userCargo == "VENDEDOR"


        if (authToken == null) {
            Toast.makeText(this, "Erro de autenticação.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // O FAB para adicionar cliente só deve ser visível e funcional se PUDER criar/editar
        // E se NÃO estiver em modo de seleção.
        if (canCreateEdit && !isSelectMode) {
            fabAddClient.visibility = View.VISIBLE
            fabAddClient.setOnClickListener { showClientFormDialog(null) }
        } else {
            fabAddClient.visibility = View.GONE
        }

        setupRecyclerView()
        fetchClients()
    }

    private fun setupRecyclerView() {
        clientListAdapter = ClientListAdapter(
            emptyList(),
            onItemClick = { client -> // Callback para clique no item inteiro
                if (isSelectMode) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("SELECTED_CLIENT_JSON", Gson().toJson(client))
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    // Em modo normal, você poderia, por exemplo, abrir uma tela de detalhes do cliente
                    // Toast.makeText(this, "Cliente ${client.nome} clicado.", Toast.LENGTH_SHORT).show()
                }
            },
            onEditClick = { client -> // Callback para o botão de editar DENTRO do item
                // A checagem de canCreateEdit e !isSelectMode já está no Adapter/ViewHolder
                showClientFormDialog(client)
            },
            onDeleteClick = { client -> // Callback para o botão de deletar DENTRO do item
                // A checagem de isAdmin e !isSelectMode já está no Adapter/ViewHolder
                showDeleteConfirmationDialog(client)
            },
            canEdit = canCreateEdit, // Passa a permissão de edição
            canDelete = isAdmin,     // Passa a permissão de deleção
            isSelectMode = isSelectMode // Informa o adapter sobre o modo atual
        )
        rvClients.adapter = clientListAdapter
    }

    private fun fetchClients() {
        pbClientList.visibility = View.VISIBLE
        tvNoClients.visibility = View.GONE
        rvClients.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.clientApi.getClients(bearerToken)
                withContext(Dispatchers.Main) {
                    pbClientList.visibility = View.GONE
                    if (response.isSuccessful) {
                        val clients = response.body()
                        if (clients.isNullOrEmpty()) {
                            tvNoClients.visibility = View.VISIBLE
                            rvClients.visibility = View.GONE
                        } else {
                            clientListAdapter.updateClients(clients)
                            tvNoClients.visibility = View.GONE
                            rvClients.visibility = View.VISIBLE
                        }
                    } else {
                        tvNoClients.visibility = View.VISIBLE
                        Log.e("FetchClientsError", "Código: ${response.code()}, Mensagem: ${response.errorBody()?.string()}")
                        Toast.makeText(this@ClientListActivity, "Erro ao buscar clientes: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbClientList.visibility = View.GONE
                    tvNoClients.visibility = View.VISIBLE
                    Log.e("FetchClientsException", "Erro: ${e.message}", e)
                    Toast.makeText(this@ClientListActivity, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showClientFormDialog(clientToEdit: ClienteDTO?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_client_form, null)
        val etClientName = dialogView.findViewById<TextInputEditText>(R.id.etDialogClientName)
        val etClientCnpj = dialogView.findViewById<TextInputEditText>(R.id.etDialogClientCnpj)
        val etClientEmail = dialogView.findViewById<TextInputEditText>(R.id.etDialogClientEmail)
        val etClientPhone = dialogView.findViewById<TextInputEditText>(R.id.etDialogClientPhone)
        val etClientAddress = dialogView.findViewById<TextInputEditText>(R.id.etDialogClientAddress)
        val pbDialog = dialogView.findViewById<ProgressBar>(R.id.pbDialogClientForm)

        val dialogTitle: String
        if (clientToEdit != null) {
            dialogTitle = "Editar Cliente"
            etClientName.setText(clientToEdit.nome)
            etClientCnpj.setText(clientToEdit.cnpj)
            etClientEmail.setText(clientToEdit.email)
            etClientPhone.setText(clientToEdit.telefone)
            etClientAddress.setText(clientToEdit.endereco)
        } else {
            dialogTitle = "Adicionar Novo Cliente"
        }

        // Aplicar Máscaras com maskara
        val cnpjMaskPattern = if (etClientCnpj.text.toString().replace("[^0-9]".toRegex(), "").length > 11) {
            "__.___.___/____-__" // CNPJ
        } else {
            "___.___.___-__"   // CPF
        }
        val cnpjMask = Mask(value = cnpjMaskPattern, character = '_', style = MaskStyle.PERSISTENT)
        cnpjCpfListener = MaskChangedListener(cnpjMask)
        etClientCnpj.addTextChangedListener(cnpjCpfListener)
        // Para alternar a máscara dinamicamente com maskara seria mais complexo,
        // exigindo remover e adicionar o listener ou usando um TextWatcher customizado
        // que decida qual máscara aplicar. A regex do backend já é flexível.

        val phoneMask = Mask(value = "(__) [9]____-____", character = '_', style = MaskStyle.PERSISTENT)
        phoneListener = MaskChangedListener(phoneMask)
        etClientPhone.addTextChangedListener(phoneListener)

        val dialog = AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(if (clientToEdit != null) "Salvar" else "Adicionar", null)
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = etClientName.text.toString().trim()
                val cnpjCpfInput = etClientCnpj.text.toString().trim()
                val email = etClientEmail.text.toString().trim()
                val phoneInput = etClientPhone.text.toString().trim()
                val address = etClientAddress.text.toString().trim()

                var isValid = true
                // ... (Validações como na resposta anterior) ...
                if (name.isEmpty()) { /* ... */ isValid = false } else { etClientName.error = null}
                // CNPJ/CPF
                val unmaskedCnpjCpf = cnpjCpfInput.replace("[^0-9]".toRegex(), "")
                val cnpjCpfPatternBackend = "^([0-9]{2}(\\.?[0-9]{3}){2}\\/?[0-9]{4}\\-?[0-9]{2})|([0-9]{3}(\\.?[0-9]{3}){2}\\-?[0-9]{2})$".toRegex()
                if (cnpjCpfInput.isEmpty()) { etClientCnpj.error = "CNPJ/CPF é obrigatório"; isValid = false
                } else if (!cnpjCpfInput.matches(cnpjCpfPatternBackend) || (unmaskedCnpjCpf.length != 11 && unmaskedCnpjCpf.length != 14)) {
                    etClientCnpj.error = "CNPJ/CPF inválido"; isValid = false
                } else { etClientCnpj.error = null }
                // Email
                if (email.isEmpty()) { etClientEmail.error = "Email é obrigatório"; isValid = false
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etClientEmail.error = "Formato de email inválido"; isValid = false
                } else { etClientEmail.error = null }
                // Telefone
                val unmaskedPhone = phoneInput.replace("[^0-9]".toRegex(), "")
                if (phoneInput.isEmpty()) { etClientPhone.error = "Telefone é obrigatório"; isValid = false
                } else if (unmaskedPhone.length !in 10..11) {
                    etClientPhone.error = "Telefone inválido (10 ou 11 dígitos com DDD)"; isValid = false
                } else { etClientPhone.error = null }
                // Endereço
                if (address.isEmpty()) { etClientAddress.error = "Endereço é obrigatório"; isValid = false
                } else { etClientAddress.error = null }


                if (!isValid) {
                    Toast.makeText(this@ClientListActivity, "Corrija os campos destacados.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                pbDialog.visibility = View.VISIBLE
                positiveButton.isEnabled = false

                val clientData = ClienteDTO(
                    id = clientToEdit?.id,
                    nome = name, telefone = phoneInput, endereco = address,
                    cnpj = cnpjCpfInput, email = email
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val bearerToken = "Bearer $authToken"
                        val response: Response<ClienteDTO> = if (clientToEdit != null) {
                            RetrofitClient.clientApi.updateClient(bearerToken, clientToEdit.id!!, clientData)
                        } else {
                            RetrofitClient.clientApi.createClient(bearerToken, clientData)
                        }
                        withContext(Dispatchers.Main) {
                            pbDialog.visibility = View.GONE
                            positiveButton.isEnabled = true
                            if (response.isSuccessful) {
                                val action = if (clientToEdit != null) "atualizado" else "criado"
                                Toast.makeText(this@ClientListActivity, "Cliente '${response.body()?.nome}' $action!", Toast.LENGTH_SHORT).show()
                                fetchClients()
                                dialog.dismiss()
                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                                showApiError(errorMsg, "Falha ao ${if (clientToEdit != null) "atualizar" else "criar"} cliente")
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            pbDialog.visibility = View.GONE
                            positiveButton.isEnabled = true
                            Log.e("ClientFormExc", "Exceção: ${e.message}", e)
                            Toast.makeText(this@ClientListActivity, "Exceção: ${e.message}", Toast.LENGTH_LONG).show()
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
        Toast.makeText(this@ClientListActivity, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun showDeleteConfirmationDialog(client: ClienteDTO) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir o cliente ${client.nome}?")
            .setPositiveButton("Excluir") { _, _ ->
                client.id?.let { deleteClient(it) }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteClient(clientId: Long) {
        pbClientList.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.clientApi.deleteClient(bearerToken, clientId)
                withContext(Dispatchers.Main) {
                    pbClientList.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(this@ClientListActivity, "Cliente excluído!", Toast.LENGTH_SHORT).show()
                        fetchClients()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                        showApiError(errorMsg, "Falha ao excluir cliente")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbClientList.visibility = View.GONE
                    Log.e("DeleteClientExc", "Exceção: ${e.message}", e)
                    Toast.makeText(this@ClientListActivity, "Exceção ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}