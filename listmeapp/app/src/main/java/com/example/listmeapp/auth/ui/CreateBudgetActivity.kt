package com.example.listmeapp.auth.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText // Para o diálogo de quantidade
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
import com.example.listmeapp.auth.ui.ClientListActivity
import com.example.listmeapp.data.api.RetrofitClient
import com.example.listmeapp.data.model.*
import com.example.listmeapp.auth.ui.ProductListActivity // Para selecionar produto
import com.example.listmeapp.auth.ui.BudgetItemsAdapter // Import o novo adapter
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.NumberFormat // Para formatar moeda
import java.util.Locale     // Para Locale

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
    private val budgetItemsDisplayList = mutableListOf<ItemOrcamentoResponseDTO>()
    private lateinit var budgetItemsAdapter: BudgetItemsAdapter

    private var authToken: String? = null
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))


    private val selectClientLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val clientJson = data?.getStringExtra("SELECTED_CLIENT_JSON")
            if (clientJson != null) {
                selectedClient = Gson().fromJson(clientJson, ClienteDTO::class.java)
                tvSelectedClientName.text = selectedClient?.nome ?: "Nenhum cliente selecionado"
                // Limpar itens se o cliente mudar? Ou perguntar ao usuário?
                // budgetItemsDisplayList.clear()
                // budgetItemsAdapter.notifyDataSetChanged()
                // updateTotalDisplay()
            }
        }
    }

    private val selectProductLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val productJson = data?.getStringExtra("SELECTED_PRODUCT_JSON")
            if (productJson != null) {
                val product = Gson().fromJson(productJson, ProdutoDTO::class.java)
                promptForProductQuantity(product)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_budget)

        // ... (findViewByIds como antes) ...
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
            createBudget()
        }
        updateTotalDisplay()
    }

    private fun setupRecyclerViewForBudgetItems() {
        budgetItemsAdapter = BudgetItemsAdapter(budgetItemsDisplayList,
            onRemoveClick = { position ->
                budgetItemsDisplayList.removeAt(position)
                budgetItemsAdapter.notifyItemRemoved(position)
                budgetItemsAdapter.notifyItemRangeChanged(position, budgetItemsDisplayList.size)
                updateTotalDisplay()
            },
            onQuantityChange = { position, newQuantity ->
                if (newQuantity > 0) {
                    val item = budgetItemsDisplayList[position]
                    val newSubtotal = (item.precoUnitario ?: BigDecimal.ZERO).multiply(BigDecimal(newQuantity))
                    budgetItemsAdapter.updateItemQuantity(position, newQuantity, newSubtotal)
                } else {
                    // Se a quantidade for 0, remover o item
                    budgetItemsDisplayList.removeAt(position)
                    budgetItemsAdapter.notifyItemRemoved(position)
                    budgetItemsAdapter.notifyItemRangeChanged(position, budgetItemsDisplayList.size)
                }
                updateTotalDisplay()
            }
        )
        rvBudgetItems.layoutManager = LinearLayoutManager(this)
        rvBudgetItems.adapter = budgetItemsAdapter
    }

    private fun promptForProductQuantity(product: ProdutoDTO) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity_input, null)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val tvProductNameQuantityDialog = dialogView.findViewById<TextView>(R.id.tvProductNameQuantityDialog)

        tvProductNameQuantityDialog.text = "Produto: ${product.nome}"

        AlertDialog.Builder(this)
            .setTitle("Definir Quantidade")
            .setView(dialogView)
            .setPositiveButton("Adicionar") { dialog, _ ->
                val quantityStr = etQuantity.text.toString()
                val quantity = quantityStr.toIntOrNull()

                if (quantity == null || quantity <= 0) {
                    Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Adicionar o item ao orçamento
                val existingItem = budgetItemsDisplayList.find { it.produtoId == product.id }
                if (existingItem != null) {
                    // TODO: Lógica para atualizar quantidade de item existente ou perguntar ao usuário
                    Toast.makeText(this, "Produto já está no orçamento. Edite a quantidade.", Toast.LENGTH_LONG).show()
                } else {
                    val subtotal = (product.preco ?: BigDecimal.ZERO).multiply(BigDecimal(quantity))
                    val newItemDisplay = ItemOrcamentoResponseDTO(
                        id = null, // ID do item do orçamento será gerado pelo backend
                        produtoId = product.id!!,
                        nomeProduto = product.nome,
                        unidadeMedidaProduto = product.unidadeMedida,
                        quantidade = quantity,
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
        if (selectedClient == null) {
            Toast.makeText(this, "Selecione um cliente.", Toast.LENGTH_SHORT).show(); return
        }
        if (budgetItemsDisplayList.isEmpty()) {
            Toast.makeText(this, "Adicione produtos ao orçamento.", Toast.LENGTH_SHORT).show(); return
        }
        val paymentMethod = etPaymentMethod.text.toString().trim()
        if (paymentMethod.isEmpty()) {
            etPaymentMethod.error = "Obrigatório"; return
        }
        etPaymentMethod.error = null

        val observations = etObservations.text.toString().trim()

        // Mapear de ItemOrcamentoResponseDTO para ItemOrcamentoRequestDTO
        val requestItems = budgetItemsDisplayList.map { displayItem ->
            ItemOrcamentoRequestDTO(
                produtoId = displayItem.produtoId,
                quantidade = displayItem.quantidade
            )
        }

        val requestDTO = OrcamentoRequestDTO(
            clienteId = selectedClient!!.id!!,
            itens = requestItems,
            formaPagamento = paymentMethod,
            observacoes = observations.ifEmpty { null },
            status = StatusOrcamento.PENDENTE.name
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
                        val novoOrcamento = response.body()
                        Toast.makeText(this@CreateBudgetActivity, "Orçamento #${novoOrcamento?.id} criado!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                        try {
                            val errorResponse = Gson().fromJson(errorMsg, MessageResponse::class.java)
                            Toast.makeText(this@CreateBudgetActivity, "Falha: ${errorResponse.message}", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@CreateBudgetActivity, "Falha: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                        Log.e("CreateBudget", "Erro API: $errorMsg")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbCreateBudget.visibility = View.GONE
                    btnSaveBudget.isEnabled = true
                    Log.e("CreateBudgetExc", "Exceção: ${e.message}", e)
                    Toast.makeText(this@CreateBudgetActivity, "Exceção: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}