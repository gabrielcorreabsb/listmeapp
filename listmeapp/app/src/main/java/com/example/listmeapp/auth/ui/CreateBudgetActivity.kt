package com.example.listmeapp.auth.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns // Para validação de email, se necessário em outros lugares (não usado aqui)
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.auth.ui.ClientListActivity // Ajuste o pacote se necessário
import com.example.listmeapp.data.api.RetrofitClient
import com.example.listmeapp.data.model.*
import com.example.listmeapp.auth.ui.ProductListActivity // Ajuste o pacote se necessário
import com.example.listmeapp.auth.ui.BudgetItemsAdapter // Ajuste o pacote do Adapter
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import retrofit2.Response

class CreateBudgetActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvSelectedClientName: TextView
    private lateinit var btnSelectClient: Button
    private lateinit var rvBudgetItems: RecyclerView
    private lateinit var btnAddProductToBudget: Button
    private lateinit var etPaymentMethod: TextInputEditText
    private lateinit var etObservations: TextInputEditText
    private lateinit var tvTotalBudgetValue: TextView
    private lateinit var btnSaveBudget: Button
    private lateinit var pbCreateBudget: ProgressBar

    private var selectedClient: ClienteDTO? = null
    private val budgetItemsDisplayList = mutableListOf<ItemOrcamentoResponseDTO>() // Para a UI
    private lateinit var budgetItemsAdapter: BudgetItemsAdapter

    private var authToken: String? = null
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    // ActivityResultLauncher para selecionar cliente
    private val selectClientLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val clientJson = data?.getStringExtra("SELECTED_CLIENT_JSON")
            if (clientJson != null) {
                try {
                    selectedClient = Gson().fromJson(clientJson, ClienteDTO::class.java)
                    tvSelectedClientName.text = selectedClient?.nome ?: "Nenhum cliente selecionado"
                } catch (e: Exception) {
                    Log.e("CreateBudget", "Erro ao parsear cliente JSON: $clientJson", e)
                    Toast.makeText(this, "Erro ao carregar dados do cliente.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ActivityResultLauncher para selecionar produto
    private val selectProductLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val productJson = data?.getStringExtra("SELECTED_PRODUCT_JSON")
            if (productJson != null) {
                try {
                    val product = Gson().fromJson(productJson, ProdutoDTO::class.java)
                    promptForProductQuantity(product) // Chama a função que agora verifica se o item existe
                } catch (e: Exception) {
                    Log.e("CreateBudget", "Erro ao parsear produto JSON: $productJson", e)
                    Toast.makeText(this, "Erro ao carregar dados do produto.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ActivityResultLauncher para editar orçamento (se você implementar a edição nesta activity)
    private var orcamentoToEdit: OrcamentoResponseDTO? = null // Para guardar o orçamento em edição

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_budget)

        toolbar = findViewById(R.id.toolbarCreateBudget)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        tvSelectedClientName = findViewById(R.id.tvSelectedClientName)
        btnSelectClient = findViewById(R.id.btnSelectClient)
        rvBudgetItems = findViewById(R.id.rvBudgetItems)
        btnAddProductToBudget = findViewById(R.id.btnAddProductToBudget)
        etPaymentMethod = findViewById(R.id.etPaymentMethod)
        etObservations = findViewById(R.id.etObservations)
        tvTotalBudgetValue = findViewById(R.id.tvTotalBudgetValue)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        pbCreateBudget = findViewById(R.id.pbCreateBudget)

        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("AUTH_TOKEN", null)

        if (authToken == null) {
            Toast.makeText(this, "Erro de autenticação.", Toast.LENGTH_LONG).show()
            finish(); return
        }

        // Verifica se está em modo de edição
        val editBudgetJson = intent.getStringExtra("EDIT_BUDGET_JSON")
        if (editBudgetJson != null) {
            try {
                orcamentoToEdit = Gson().fromJson(editBudgetJson, OrcamentoResponseDTO::class.java)
                populateFormForEdit()
                toolbar.title = "Editar Orçamento #${orcamentoToEdit?.id}"
                btnSaveBudget.text = "Salvar Alterações"
            } catch (e: Exception) {
                Log.e("CreateBudget", "Erro ao parsear orçamento para edição: $editBudgetJson", e)
                Toast.makeText(this, "Erro ao carregar dados para edição.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } else {
            updateTotalDisplay() // Inicializa o total como R$ 0,00 para novo orçamento
        }

        setupRecyclerViewForBudgetItems()

        btnSelectClient.setOnClickListener {
            val intent = Intent(this, ClientListActivity::class.java)
            intent.putExtra("SELECT_MODE", true)
            selectClientLauncher.launch(intent)
        }

        btnAddProductToBudget.setOnClickListener {
            val intent = Intent(this, ProductListActivity::class.java)
            intent.putExtra("SELECT_MODE", true)
            selectProductLauncher.launch(intent)
        }

        btnSaveBudget.setOnClickListener {
            if (orcamentoToEdit != null) {
                updateBudget() // Chama a função de atualizar
            } else {
                createBudget() // Chama a função de criar
            }
        }
    }

    private fun populateFormForEdit() {
        orcamentoToEdit?.let { budget ->
            selectedClient = budget.cliente
            tvSelectedClientName.text = budget.cliente.nome
            etPaymentMethod.setText(budget.formaPagamento)
            etObservations.setText(budget.observacoes)
            // Popula a lista de itens para exibição (e para o adapter)
            budgetItemsDisplayList.clear()
            budgetItemsDisplayList.addAll(budget.itens)
            // budgetItemsAdapter.notifyDataSetChanged() // Será chamado em setupRecyclerView
            updateTotalDisplay()
        }
    }


    private fun setupRecyclerViewForBudgetItems() {
        budgetItemsAdapter = BudgetItemsAdapter(budgetItemsDisplayList,
            onRemoveClick = { position ->
                if (position >= 0 && position < budgetItemsDisplayList.size) {
                    budgetItemsDisplayList.removeAt(position)
                    budgetItemsAdapter.notifyItemRemoved(position)
                    budgetItemsAdapter.notifyItemRangeChanged(position, budgetItemsDisplayList.size - position)
                    updateTotalDisplay()
                }
            },
            onQuantityChange = { position, newQuantity ->
                if (position >= 0 && position < budgetItemsDisplayList.size) {
                    if (newQuantity > 0) {
                        val item = budgetItemsDisplayList[position]
                        val newSubtotal = (item.precoUnitario ?: BigDecimal.ZERO).multiply(BigDecimal(newQuantity))
                        budgetItemsAdapter.updateItemQuantity(position, newQuantity, newSubtotal)
                    } else {
                        budgetItemsDisplayList.removeAt(position)
                        budgetItemsAdapter.notifyItemRemoved(position)
                        budgetItemsAdapter.notifyItemRangeChanged(position, budgetItemsDisplayList.size - position)
                    }
                    updateTotalDisplay()
                }
            }
        )
        rvBudgetItems.layoutManager = LinearLayoutManager(this)
        rvBudgetItems.adapter = budgetItemsAdapter
        // Se estiver editando, o adapter já será populado com os itens em populateFormForEdit e notificado
        // Se for novo, a lista estará vazia.
    }

    private fun promptForProductQuantity(product: ProdutoDTO) {
        val existingItemIndex = budgetItemsDisplayList.indexOfFirst { it.produtoId == product.id }

        if (existingItemIndex != -1) {
            val currentItem = budgetItemsDisplayList[existingItemIndex]
            AlertDialog.Builder(this)
                .setTitle("Produto já Adicionado")
                .setMessage("${product.nome} já está no orçamento com quantidade ${currentItem.quantidade}. O que deseja fazer?")
                .setPositiveButton("Somar Nova Qtd") { _, _ ->
                    showQuantityInputDialog(product, currentItem.quantidade, existingItemIndex, true)
                }
                .setNeutralButton("Substituir Qtd") { _, _ ->
                    showQuantityInputDialog(product, 0, existingItemIndex, false)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            showQuantityInputDialog(product, 1, -1, false) // Padrão 1 para novo item
        }
    }

    private fun showQuantityInputDialog(product: ProdutoDTO, existingOrInitialQuantity: Int, itemPosition: Int, isSumMode: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity_input, null)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val tvProductNameQuantityDialog = dialogView.findViewById<TextView>(R.id.tvProductNameQuantityDialog)

        tvProductNameQuantityDialog.text = "Produto: ${product.nome}"
        if (!isSumMode && itemPosition != -1) { // Editando quantidade de item existente
            etQuantity.setText(existingOrInitialQuantity.toString())
            etQuantity.hint = "Nova quantidade total"
        } else if (isSumMode) { // Somando à quantidade de item existente
            etQuantity.hint = "Adicionar mais X unidades"
        } else { // Novo item
            etQuantity.setText(existingOrInitialQuantity.toString()) // Padrão 1
            etQuantity.hint = "Quantidade"
        }
        etQuantity.requestFocus() // Foca no campo
        etQuantity.selectAll()    // Seleciona o texto para fácil substituição

        AlertDialog.Builder(this)
            .setTitle(if(isSumMode) "Adicionar Quantidade" else "Definir Quantidade")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { dialog, _ ->
                val quantityStr = etQuantity.text.toString()
                val quantityInput = quantityStr.toIntOrNull()

                if (quantityInput == null || quantityInput <= 0) {
                    Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val finalQuantity: Int
                if (itemPosition != -1) { // Item existente
                    finalQuantity = if (isSumMode) {
                        budgetItemsDisplayList[itemPosition].quantidade + quantityInput // Soma à quantidade atual do item na lista
                    } else {
                        quantityInput // Substitui
                    }
                    if (finalQuantity > 0) {
                        val item = budgetItemsDisplayList[itemPosition]
                        val newSubtotal = (item.precoUnitario ?: BigDecimal.ZERO).multiply(BigDecimal(finalQuantity))
                        budgetItemsAdapter.updateItemQuantity(itemPosition, finalQuantity, newSubtotal)
                    } else {
                        budgetItemsDisplayList.removeAt(itemPosition)
                        budgetItemsAdapter.notifyItemRemoved(itemPosition)
                        budgetItemsAdapter.notifyItemRangeChanged(itemPosition, budgetItemsDisplayList.size - itemPosition)
                    }
                } else { // Novo item
                    finalQuantity = quantityInput
                    val subtotal = (product.preco ?: BigDecimal.ZERO).multiply(BigDecimal(finalQuantity))
                    val newItemDisplay = ItemOrcamentoResponseDTO(
                        id = null,
                        produtoId = product.id!!,
                        nomeProduto = product.nome,
                        unidadeMedidaProduto = product.unidadeMedida,
                        quantidade = finalQuantity,
                        precoUnitario = product.preco,
                        valorTotalItem = subtotal
                    )
                    budgetItemsAdapter.addItem(newItemDisplay)
                }
                updateTotalDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateTotalDisplay() {
        val total = budgetItemsDisplayList.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.valorTotalItem ?: BigDecimal.ZERO)
        }
        tvTotalBudgetValue.text = "Total: ${currencyFormat.format(total)}"
    }

    private fun createBudget() {
        // ... (lógica de validação e criação do requestDTO como antes) ...
        if (selectedClient == null) { Toast.makeText(this, "Selecione um cliente.", Toast.LENGTH_SHORT).show(); return }
        if (budgetItemsDisplayList.isEmpty()) { Toast.makeText(this, "Adicione produtos.", Toast.LENGTH_SHORT).show(); return }
        val paymentMethod = etPaymentMethod.text.toString().trim()
        if (paymentMethod.isEmpty()) { etPaymentMethod.error = "Obrigatório"; return }
        etPaymentMethod.error = null
        val observations = etObservations.text.toString().trim()

        val requestItems = budgetItemsDisplayList.map { ItemOrcamentoRequestDTO(it.produtoId, it.quantidade) }
        val requestDTO = OrcamentoRequestDTO(
            selectedClient!!.id!!, requestItems, paymentMethod,
            observations.ifEmpty { null }, StatusOrcamento.PENDENTE.name
        )

        pbCreateBudget.visibility = View.VISIBLE
        btnSaveBudget.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.orcamentoApi.createOrcamento(bearerToken, requestDTO)
                withContext(Dispatchers.Main) {
                    pbCreateBudget.visibility = View.GONE
                    btnSaveBudget.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreateBudgetActivity, "Orçamento #${response.body()?.id} criado!", Toast.LENGTH_LONG).show()
                        setResult(Activity.RESULT_OK) // Informa que a criação foi bem-sucedida
                        finish()
                    } else {
                        // ... (tratamento de erro da API) ...
                        val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                        try {
                            val errorResponse = Gson().fromJson(errorMsg, MessageResponse::class.java)
                            Toast.makeText(this@CreateBudgetActivity, "Falha ao criar: ${errorResponse.message}", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@CreateBudgetActivity, "Falha ao criar: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                        Log.e("CreateBudget", "Erro API: $errorMsg")
                    }
                }
            } catch (e: Exception) {
                // ... (tratamento de exceção de rede) ...
                withContext(Dispatchers.Main) {
                    pbCreateBudget.visibility = View.GONE
                    btnSaveBudget.isEnabled = true
                    Log.e("CreateBudgetExc", "Exceção: ${e.message}", e)
                    Toast.makeText(this@CreateBudgetActivity, "Exceção: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateBudget() {
        if (selectedClient == null || orcamentoToEdit == null) {
            Toast.makeText(this, "Erro: Dados do cliente ou orçamento ausentes para atualização.", Toast.LENGTH_LONG).show()
            return
        }
        if (budgetItemsDisplayList.isEmpty()) {
            Toast.makeText(this, "Adicione produtos ao orçamento.", Toast.LENGTH_SHORT).show()
            return
        }
        val paymentMethod = etPaymentMethod.text.toString().trim()
        if (paymentMethod.isEmpty()) {
            etPaymentMethod.error = "Obrigatório"
            return
        }
        etPaymentMethod.error = null
        val observations = etObservations.text.toString().trim()

        val requestItems = budgetItemsDisplayList.map { ItemOrcamentoRequestDTO(it.produtoId, it.quantidade) }
        // O status pode ser diferente ao editar, mas para este exemplo, vamos manter PENDENTE
        // Você pode querer um Spinner para o status na edição se ele puder ser alterado.
        val requestDTO = OrcamentoRequestDTO(
            clienteId = selectedClient!!.id!!,
            itens = requestItems,
            formaPagamento = paymentMethod,
            observacoes = observations.ifEmpty { null },
            status = orcamentoToEdit?.status ?: StatusOrcamento.PENDENTE.name // Usa o status atual ou PENDENTE
        )

        pbCreateBudget.visibility = View.VISIBLE
        btnSaveBudget.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.orcamentoApi.updateOrcamento(bearerToken, orcamentoToEdit!!.id, requestDTO)
                withContext(Dispatchers.Main) {
                    pbCreateBudget.visibility = View.GONE
                    btnSaveBudget.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreateBudgetActivity, "Orçamento #${response.body()?.id} atualizado!", Toast.LENGTH_LONG).show()
                        setResult(Activity.RESULT_OK) // Informa que a edição foi bem-sucedida
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                        try {
                            val errorResponse = Gson().fromJson(errorMsg, MessageResponse::class.java)
                            Toast.makeText(this@CreateBudgetActivity, "Falha ao atualizar: ${errorResponse.message}", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@CreateBudgetActivity, "Falha ao atualizar: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                        Log.e("UpdateBudget", "Erro API: $errorMsg")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbCreateBudget.visibility = View.GONE
                    btnSaveBudget.isEnabled = true
                    Log.e("UpdateBudgetExc", "Exceção: ${e.message}", e)
                    Toast.makeText(this@CreateBudgetActivity, "Exceção: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }


    }
}