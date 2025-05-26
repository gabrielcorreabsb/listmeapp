package com.example.listmeapp.auth.ui // Sugestão: mover para pacote client.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns // Para validação de Email
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager // Import para LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.api.RetrofitClient
import com.example.listmeapp.data.model.ClienteDTO
import com.example.listmeapp.data.model.MessageResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.santalu.maskara.Mask
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
    private var isSelectMode: Boolean = false

    private lateinit var phoneListener: MaskChangedListener // Mantido para telefone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_list)

        toolbar = findViewById(R.id.toolbarClientList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            if (isSelectMode) {
                setResult(Activity.RESULT_CANCELED)
            }
            onBackPressedDispatcher.onBackPressed()
        }

        rvClients = findViewById(R.id.rvClients)
        pbClientList = findViewById(R.id.pbClientList)
        tvNoClients = findViewById(R.id.tvNoClients)
        fabAddClient = findViewById(R.id.fabAddClient)

        isSelectMode = intent.getBooleanExtra("SELECT_MODE", false)

        if (isSelectMode) {
            toolbar.title = "Selecionar Cliente"
            fabAddClient.visibility = View.GONE
        } else {
            toolbar.title = "Gerenciar Clientes"
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
            onItemClick = { client ->
                if (isSelectMode) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("SELECTED_CLIENT_JSON", Gson().toJson(client))
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            },
            onEditClick = { client ->
                if (canCreateEdit && !isSelectMode) showClientFormDialog(client)
            },
            onDeleteClick = { client ->
                if (isAdmin && !isSelectMode) showDeleteConfirmationDialog(client)
            },
            canEdit = canCreateEdit,
            canDelete = isAdmin,
            isSelectMode = isSelectMode
        )
        rvClients.layoutManager = LinearLayoutManager(this) // Adicionado LayoutManager
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

        // Aplicar Máscara de Telefone com maskara
        val phoneMask = Mask(value = "(__) [9]____-____", character = '_', style = MaskStyle.PERSISTENT)
        phoneListener = MaskChangedListener(phoneMask)
        etClientPhone.addTextChangedListener(phoneListener)

        // Aplicar TextWatcher customizado para CNPJ/CPF
        etClientCnpj.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val cnpjMask = "##.###.###/####-##"
            private val cpfMask = "###.###.###-##"
            private var old = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString().replace("[^0-9]".toRegex(), "")
                if (str == old || isUpdating) {
                    old = str
                    return
                }
                isUpdating = true
                val maskToApply = if (str.length > 11) cnpjMask else cpfMask
                var masked = ""
                var i = 0
                for (m_char in maskToApply.toCharArray()) {
                    if (i >= str.length) break
                    if (m_char == '#') { // Usando '#' como placeholder da máscara
                        masked += str[i]
                        i++
                    } else {
                        masked += m_char
                    }
                }
                etClientCnpj.setText(masked)
                etClientCnpj.setSelection(masked.length)
                isUpdating = false
                old = str
            }
        })


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
                val cnpjCpfInput = etClientCnpj.text.toString().trim() // <<-- PRIMEIRA DECLARAÇÃO (E ÚNICA AGORA)
                val email = etClientEmail.text.toString().trim()
                val phoneInput = etClientPhone.text.toString().trim()
                val address = etClientAddress.text.toString().trim()

                var isValid = true

                if (name.isEmpty()) {
                    etClientName.error = "Nome é obrigatório"; isValid = false
                } else { etClientName.error = null }

                val unmaskedCnpjCpf = cnpjCpfInput.replace("[^0-9]".toRegex(), "")
                val cnpjCpfPatternBackend = "^([0-9]{2}(\\.?[0-9]{3}){2}\\/?[0-9]{4}\\-?[0-9]{2})|([0-9]{3}(\\.?[0-9]{3}){2}\\-?[0-9]{2})$".toRegex()

                if (cnpjCpfInput.isEmpty()) {
                    etClientCnpj.error = "CNPJ/CPF é obrigatório"; isValid = false
                } else if (!cnpjCpfInput.matches(cnpjCpfPatternBackend) || (unmaskedCnpjCpf.length != 11 && unmaskedCnpjCpf.length != 14)) {
                    etClientCnpj.error = "CNPJ/CPF inválido"; isValid = false
                } else { etClientCnpj.error = null }

                if (email.isEmpty()) {
                    etClientEmail.error = "Email é obrigatório"; isValid = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // Usar android.util.Patterns
                    etClientEmail.error = "Formato de email inválido"; isValid = false
                } else { etClientEmail.error = null }

                val unmaskedPhone = phoneInput.replace("[^0-9]".toRegex(), "")
                if (phoneInput.isEmpty()) {
                    etClientPhone.error = "Telefone é obrigatório"; isValid = false
                } else if (unmaskedPhone.length !in 10..11) {
                    etClientPhone.error = "Telefone inválido (10 ou 11 dígitos com DDD)"; isValid = false
                } else { etClientPhone.error = null }

                if (address.isEmpty()) {
                    etClientAddress.error = "Endereço é obrigatório"; isValid = false
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