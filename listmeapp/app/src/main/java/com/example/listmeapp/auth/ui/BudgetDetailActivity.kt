package com.example.listmeapp.auth.ui // Ou o pacote correto da sua Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.api.RetrofitClient

import com.example.listmeapp.data.model.MessageResponse
import com.example.listmeapp.data.model.OrcamentoResponseDTO
import com.example.listmeapp.data.model.StatusOrcamento

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class BudgetDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvBudgetIdValue: TextView
    private lateinit var tvClientName: TextView
    private lateinit var tvClientContact: TextView
    private lateinit var tvEmployeeName: TextView
    private lateinit var tvBudgetDate: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var rvDetailBudgetItems: RecyclerView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var tvObservations: TextView
    private lateinit var tvTotalValue: TextView
    private lateinit var btnEditBudget: Button
    private lateinit var btnShareBudget: Button
    private lateinit var btnDeleteBudget: Button
    private lateinit var pbBudgetDetail: ProgressBar

    private lateinit var layoutStatusActions: LinearLayout
    private lateinit var btnMarkAsPaid: Button
    private lateinit var btnMarkAsSent: Button
    private lateinit var btnCancelBudget: Button

    private lateinit var budgetDetailItemsAdapter: BudgetDetailItemsAdapter
    private var budgetId: Long = -1L
    private var currentBudget: OrcamentoResponseDTO? = null
    private var authToken: String? = null
    private var isAdmin: Boolean = false
    private var userCargo: String? = null
    private var canPerformStatusActions: Boolean = false

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val outputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val inputDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private val editBudgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Orçamento atualizado. Recarregando...", Toast.LENGTH_SHORT).show()
            fetchBudgetDetails()
            setResult(Activity.RESULT_OK)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_detail)

        initializeViews()
        setupToolbar()
        loadInitialData()

        if (authToken == null) { // Verificação antecipada de token
            Toast.makeText(this, "Erro de autenticação. Faça login novamente.", Toast.LENGTH_LONG).show()
            finishAffinity() // Fecha todas as activities do app e volta para o launcher
            // Considere iniciar LoginActivity aqui também se quiser um fluxo mais direto
            // startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        if (budgetId == -1L) {
            Toast.makeText(this, "ID do orçamento inválido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        fetchBudgetDetails() // Busca os dados e depois configura os listeners que dependem de currentBudget
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbarBudgetDetail)
        tvBudgetIdValue = findViewById(R.id.tvDetailBudgetIdValue)
        tvClientName = findViewById(R.id.tvDetailClientName)
        tvClientContact = findViewById(R.id.tvDetailClientContact)
        tvEmployeeName = findViewById(R.id.tvDetailEmployeeName)
        tvBudgetDate = findViewById(R.id.tvDetailBudgetDate)
        tvBudgetStatus = findViewById(R.id.tvDetailBudgetStatus)
        rvDetailBudgetItems = findViewById(R.id.rvDetailBudgetItems)
        tvPaymentMethod = findViewById(R.id.tvDetailPaymentMethod)
        tvObservations = findViewById(R.id.tvDetailObservations)
        tvTotalValue = findViewById(R.id.tvDetailTotalValue)
        btnEditBudget = findViewById(R.id.btnEditBudget)
        btnShareBudget = findViewById(R.id.btnShareBudget)
        btnDeleteBudget = findViewById(R.id.btnDeleteBudget)
        pbBudgetDetail = findViewById(R.id.pbBudgetDetail)
        layoutStatusActions = findViewById(R.id.layoutStatusActions)
        btnMarkAsPaid = findViewById(R.id.btnMarkAsPaid)
        btnMarkAsSent = findViewById(R.id.btnMarkAsSent)
        btnCancelBudget = findViewById(R.id.btnCancelBudget)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun loadInitialData() {
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("AUTH_TOKEN", null)
        userCargo = sharedPreferences.getString("USER_CARGO", null)
        isAdmin = userCargo == "ADMIN"
        canPerformStatusActions = userCargo == "ADMIN" || userCargo == "VENDEDOR"
        budgetId = intent.getLongExtra("BUDGET_ID", -1L)
    }

    private fun setupActionListeners() {
        btnEditBudget.setOnClickListener {
            currentBudget?.let { budget ->
                if ((isAdmin || userCargo == "VENDEDOR") && budget.status.equals(StatusOrcamento.PENDENTE.name, ignoreCase = true)) {
                    val intent = Intent(this, CreateBudgetActivity::class.java) // Verifique o pacote de CreateBudgetActivity
                    intent.putExtra("EDIT_BUDGET_JSON", Gson().toJson(budget))
                    editBudgetLauncher.launch(intent)
                } else {
                    Toast.makeText(this, "Este orçamento não pode mais ser editado.", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Dados do orçamento não carregados.", Toast.LENGTH_SHORT).show()
        }

        btnShareBudget.setOnClickListener {
            Log.d("ShareBudget", "Botão Compartilhar Clicado.")
            currentBudget?.let { budget ->
                Log.d("ShareBudget", "currentBudget NÃO é nulo, ID: ${budget.id}. Chamando generateAndShareReceipt...")
                generateAndShareReceipt(budget)
            } ?: run {
                Log.e("ShareBudget", "currentBudget É NULO! Não pode compartilhar.")
                Toast.makeText(this, "Dados do orçamento não carregados para compartilhar.", Toast.LENGTH_SHORT).show()
            }
        }

        btnDeleteBudget.setOnClickListener {
            if (isAdmin && currentBudget != null) {
                showDeleteConfirmationDialog()
            } else if (currentBudget == null) {
                Toast.makeText(this, "Dados do orçamento não carregados.", Toast.LENGTH_SHORT).show()
            }
        }

        btnMarkAsPaid.setOnClickListener {
            currentBudget?.let { updateBudgetStatus(it.id, StatusOrcamento.CONCLUIDO) }
                ?: Toast.makeText(this, "Dados do orçamento não carregados.", Toast.LENGTH_SHORT).show()
        }
        btnMarkAsSent.setOnClickListener {
            currentBudget?.let { updateBudgetStatus(it.id, StatusOrcamento.ENVIADO) }
                ?: Toast.makeText(this, "Dados do orçamento não carregados.", Toast.LENGTH_SHORT).show()
        }
        btnCancelBudget.setOnClickListener {
            currentBudget?.let {
                AlertDialog.Builder(this)
                    .setTitle("Cancelar Orçamento")
                    .setMessage("Tem certeza que deseja cancelar o orçamento #${it.id}?")
                    .setPositiveButton("Sim, Cancelar") { _, _ ->
                        updateBudgetStatus(it.id, StatusOrcamento.CANCELADO)
                    }
                    .setNegativeButton("Não", null)
                    .show()
            } ?: Toast.makeText(this, "Dados do orçamento não carregados.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupRecyclerView() {
        budgetDetailItemsAdapter = BudgetDetailItemsAdapter(emptyList())
        rvDetailBudgetItems.layoutManager = LinearLayoutManager(this)
        rvDetailBudgetItems.adapter = budgetDetailItemsAdapter
        rvDetailBudgetItems.isNestedScrollingEnabled = false
    }

    private fun fetchBudgetDetails() {
        pbBudgetDetail.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.orcamentoApi.getOrcamentoById(bearerToken, budgetId)
                withContext(Dispatchers.Main) {
                    pbBudgetDetail.visibility = View.GONE
                    if (response.isSuccessful) {
                        currentBudget = response.body()
                        if (currentBudget != null) {
                            populateUi(currentBudget!!)
                            setupActionListeners() // Configura os listeners APÓS currentBudget ser populado
                        } else {
                            Log.e("BudgetDetail", "Corpo da resposta nulo ao buscar detalhes.")
                            Toast.makeText(this@BudgetDetailActivity, "Não foi possível carregar os dados do orçamento.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    } else {
                        Log.e("BudgetDetail", "Erro ao buscar detalhes: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(this@BudgetDetailActivity, "Erro ao carregar orçamento: ${response.code()}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbBudgetDetail.visibility = View.GONE
                    Log.e("BudgetDetailExc", "Exceção: ${e.message}", e)
                    Toast.makeText(this@BudgetDetailActivity, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun populateUi(budget: OrcamentoResponseDTO) {
        supportActionBar?.title = "Orçamento #${budget.id}"
        tvBudgetIdValue.text = "#${budget.id}"
        tvClientName.text = budget.cliente.nome
        tvClientContact.text = "Telefone: ${budget.cliente.telefone} | CNPJ/CPF: ${budget.cliente.cnpj ?: "N/A"}"
        tvEmployeeName.text = budget.funcionario.nome
        try {
            val parsedDate = LocalDateTime.parse(budget.dataOrcamento, inputDateFormatter)
            tvBudgetDate.text = outputDateFormatter.format(parsedDate)
        } catch (e: DateTimeParseException) {
            tvBudgetDate.text = budget.dataOrcamento
            Log.e("BudgetDetail", "Erro ao parsear data do orçamento: ${budget.dataOrcamento}", e)
        } catch (e: Exception){
            tvBudgetDate.text = budget.dataOrcamento
            Log.e("BudgetDetail", "Erro geral ao parsear data: ${budget.dataOrcamento}", e)
        }

        val statusText = budget.status.replace("_", " ").capitalizeWords()
        tvBudgetStatus.text = statusText
        val statusEnum = try { StatusOrcamento.valueOf(budget.status.uppercase()) } catch (e: Exception) { null }

        val statusColor = when (statusEnum) {
            StatusOrcamento.PENDENTE -> ContextCompat.getColor(this, R.color.status_pending)
            StatusOrcamento.ENVIADO -> ContextCompat.getColor(this, R.color.status_sent)
            StatusOrcamento.APROVADO -> ContextCompat.getColor(this, R.color.status_approved)
            StatusOrcamento.REJEITADO -> ContextCompat.getColor(this, R.color.status_rejected)
            StatusOrcamento.CONCLUIDO -> ContextCompat.getColor(this, R.color.status_completed)
            StatusOrcamento.CANCELADO -> ContextCompat.getColor(this, R.color.status_cancelled)
            else -> Color.DKGRAY
        }
        tvBudgetStatus.setBackgroundColor(statusColor)
        val darkTextStatuses = listOf(StatusOrcamento.PENDENTE, StatusOrcamento.ENVIADO, StatusOrcamento.APROVADO)
        if (statusEnum in darkTextStatuses) {
            tvBudgetStatus.setTextColor(Color.BLACK)
        } else {
            tvBudgetStatus.setTextColor(Color.WHITE)
        }

        tvPaymentMethod.text = budget.formaPagamento
        tvObservations.text = budget.observacoes ?: "Nenhuma."
        tvTotalValue.text = currencyFormat.format(budget.valorTotal)

        budgetDetailItemsAdapter.updateItems(budget.itens)

        // Lógica de visibilidade dos botões
        layoutStatusActions.visibility = if (canPerformStatusActions) View.VISIBLE else View.GONE
        btnMarkAsPaid.visibility = View.GONE
        btnMarkAsSent.visibility = View.GONE
        btnCancelBudget.visibility = View.GONE

        if (canPerformStatusActions) {
            when (statusEnum) {
                StatusOrcamento.PENDENTE -> {
                    btnMarkAsSent.visibility = View.VISIBLE
                    btnMarkAsPaid.visibility = View.VISIBLE
                    btnCancelBudget.visibility = View.VISIBLE
                }
                StatusOrcamento.ENVIADO, StatusOrcamento.APROVADO -> {
                    btnMarkAsPaid.visibility = View.VISIBLE
                    btnCancelBudget.visibility = View.VISIBLE
                }
                else -> { /* Sem ações visíveis para CONCLUIDO, REJEITADO, CANCELADO */ }
            }
        }

        val canActuallyEditThisBudget = (isAdmin || userCargo == "VENDEDOR") && budget.status.equals(StatusOrcamento.PENDENTE.name, ignoreCase = true)
        btnEditBudget.visibility = if (canActuallyEditThisBudget) View.VISIBLE else View.GONE
        btnDeleteBudget.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }

    private fun updateBudgetStatus(orcamentoId: Long, novoStatus: StatusOrcamento) {
        if (authToken == null) { Toast.makeText(this, "Não autenticado", Toast.LENGTH_SHORT).show(); return }
        pbBudgetDetail.visibility = View.VISIBLE
        btnMarkAsPaid.isEnabled = false; btnMarkAsSent.isEnabled = false; btnCancelBudget.isEnabled = false
        btnEditBudget.isEnabled = false; btnDeleteBudget.isEnabled = false; btnShareBudget.isEnabled = false


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.orcamentoApi.updateOrcamentoStatus(bearerToken, orcamentoId, novoStatus.name)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        currentBudget = response.body() // Atualiza currentBudget com a resposta da API
                        if(currentBudget != null){
                            populateUi(currentBudget!!) // Repopula a UI com os dados atualizados
                            Toast.makeText(this@BudgetDetailActivity, "Status atualizado para ${novoStatus.name.capitalizeWords()}", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK) // Notifica a lista para atualizar
                        } else {
                            Toast.makeText(this@BudgetDetailActivity, "Resposta do servidor vazia após atualizar status.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                        showApiError(errorMsg, "Falha ao atualizar status")
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateStatusExc", "Exceção: ${e.message}", e)
                Toast.makeText(this@BudgetDetailActivity, "Exceção: ${e.message}", Toast.LENGTH_LONG).show()
            } finally { // Garante que o ProgressBar e botões sejam reajustados
                withContext(Dispatchers.Main){
                    pbBudgetDetail.visibility = View.GONE
                    btnMarkAsPaid.isEnabled = true; btnMarkAsSent.isEnabled = true; btnCancelBudget.isEnabled = true
                    btnEditBudget.isEnabled = true; btnDeleteBudget.isEnabled = true; btnShareBudget.isEnabled = true
                    // A visibilidade dos botões de status e editar/deletar será reajustada por populateUi
                    currentBudget?.let { populateUi(it) }
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        currentBudget?.let { budget ->
            AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir o orçamento #${budget.id}?")
                .setPositiveButton("Excluir") { _, _ -> deleteBudget(budget.id) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun deleteBudget(id: Long) {
        if (authToken == null) { Toast.makeText(this, "Não autenticado", Toast.LENGTH_SHORT).show(); return }
        pbBudgetDetail.visibility = View.VISIBLE
        // Desabilitar botões para evitar múltiplos cliques
        btnEditBudget.isEnabled = false; btnDeleteBudget.isEnabled = false; btnShareBudget.isEnabled = false
        btnMarkAsPaid.isEnabled = false; btnMarkAsSent.isEnabled = false; btnCancelBudget.isEnabled = false


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.orcamentoApi.deleteOrcamento(bearerToken, id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BudgetDetailActivity, "Orçamento excluído!", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                        showApiError(errorMsg, "Falha ao excluir")
                    }
                }
            } catch (e: Exception) {
                Log.e("DeleteBudgetExc", "Exceção: ${e.message}", e)
                Toast.makeText(this@BudgetDetailActivity, "Exceção: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                withContext(Dispatchers.Main){
                    pbBudgetDetail.visibility = View.GONE
                    // Botões não precisam ser reabilitados aqui, pois a activity vai fechar.
                    // Se não fosse fechar, reabilitaria.
                }
            }
        }
    }

    private fun generateAndShareReceipt(budget: OrcamentoResponseDTO) {
        Log.d("ShareBudget", "Iniciando generateAndShareReceipt para orçamento ID: ${budget.id}")
        val inflater = LayoutInflater.from(this)
        val receiptView = inflater.inflate(R.layout.layout_budget_receipt, null, false)

        populateReceiptView(receiptView, budget)
        Log.d("ShareBudget", "View do recibo populada.")

        val bitmap = createBitmapFromView(receiptView)
        if (bitmap == null) {
            Log.e("ShareBudget", "Bitmap é nulo. Não é possível continuar.")
            Toast.makeText(this, "Erro ao gerar imagem do comprovante (bitmap nulo).", Toast.LENGTH_LONG).show()
            return
        }
        Log.d("ShareBudget", "Bitmap criado: ${bitmap.width}x${bitmap.height}")

        val imageFileName = "orcamento_${budget.id}_${System.currentTimeMillis()}.png"
        val imageFile = saveBitmapToFile(bitmap, imageFileName)

        if (imageFile != null) {
            Log.d("ShareBudget", "Arquivo de imagem salvo: ${imageFile.absolutePath}")
            shareImageFile(imageFile)
        } else {
            Log.e("ShareBudget", "imageFile é nulo. Erro ao salvar.")
            Toast.makeText(this, "Erro ao salvar comprovante como arquivo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateReceiptView(view: View, budget: OrcamentoResponseDTO) {
        // ... (código de populateReceiptView como antes, certifique-se que os IDs estão corretos) ...
        view.findViewById<TextView>(R.id.receipt_budget_id).text = "Orçamento Nº: ${budget.id}"
        try {
            val parsedDate = org.threeten.bp.LocalDateTime.parse(budget.dataOrcamento, inputDateFormatter)
            view.findViewById<TextView>(R.id.receipt_date).text = "Data: ${outputDateFormatter.format(parsedDate)}"
        } catch (e: Exception) { view.findViewById<TextView>(R.id.receipt_date).text = "Data: ${budget.dataOrcamento}" }
        view.findViewById<TextView>(R.id.receipt_client_name).text = budget.cliente.nome
        view.findViewById<TextView>(R.id.receipt_client_document).text = "CNPJ/CPF: ${budget.cliente.cnpj ?: "N/A"}"
        view.findViewById<TextView>(R.id.receipt_client_phone).text = "Telefone: ${budget.cliente.telefone}"
        view.findViewById<TextView>(R.id.receipt_client_address).text = budget.cliente.endereco
        view.findViewById<TextView>(R.id.receipt_employee_name).text = budget.funcionario.nome
        val itemsContainer = view.findViewById<LinearLayout>(R.id.receipt_items_container)
        itemsContainer.removeAllViews()
        val itemInflater = LayoutInflater.from(this)
        for (item in budget.itens) {
            val itemViewLayout = itemInflater.inflate(R.layout.item_budget_product_detail, itemsContainer, false)
            itemViewLayout.findViewById<TextView>(R.id.tvDetailItemProductName).text = item.nomeProduto ?: "Produto"
            val priceDetailText = "Qtd: ${item.quantidade} ${item.unidadeMedidaProduto?.lowercase() ?: "un"} x ${currencyFormat.format(item.precoUnitario ?: BigDecimal.ZERO)} = ${currencyFormat.format(item.valorTotalItem ?: BigDecimal.ZERO)}"
            itemViewLayout.findViewById<TextView>(R.id.tvDetailItemPriceQuantity).text = priceDetailText
            itemsContainer.addView(itemViewLayout)
        }
        view.findViewById<TextView>(R.id.receipt_payment_method).text = budget.formaPagamento
        view.findViewById<TextView>(R.id.receipt_total_value).text = currencyFormat.format(budget.valorTotal)
        val observationsTv = view.findViewById<TextView>(R.id.receipt_observations)
        if (!budget.observacoes.isNullOrEmpty()) {
            observationsTv.text = "Observações: ${budget.observacoes}"
            observationsTv.visibility = View.VISIBLE
        } else { observationsTv.visibility = View.GONE }
    }

    private fun createBitmapFromView(view: View): Bitmap? {
        val desiredWidth = 1080 // Largura desejada para a imagem
        view.measure(
            View.MeasureSpec.makeMeasureSpec(desiredWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        // Força o layout com as dimensões medidas ou um mínimo se as medidas forem zero
        val measuredWidth = if (view.measuredWidth > 0) view.measuredWidth else desiredWidth
        val measuredHeight = view.measuredHeight.coerceAtLeast(200) // Altura mínima de 200, ajuste

        view.layout(0, 0, measuredWidth, measuredHeight)

        if (view.width <= 0 || view.height <= 0) {
            Log.e("CreateBitmap", "Dimensões finais da view inválidas após layout: L=${view.width}, A=${view.height}")
            return null
        }

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        Log.d("CreateBitmap", "Bitmap criado: ${bitmap.width}x${bitmap.height}")
        return bitmap
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String): File? {
        val cachePath = File(applicationContext.cacheDir, "images")
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }
        try {
            val file = File(cachePath, fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream) // Qualidade 95
            stream.flush()
            stream.close()
            Log.d("SaveBitmap", "Imagem salva em: ${file.absolutePath}")
            return file
        } catch (e: IOException) {
            Log.e("SaveBitmap", "Erro ao salvar bitmap: ${e.message}", e)
        }
        return null
    }

    private fun shareImageFile(imageFile: File) {
        val fileUri: Uri? = try {
            FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider", // Ex: com.example.listmeapp.provider
                imageFile
            )
        } catch (e: IllegalArgumentException) {
            Log.e("FileProviderError", "Erro ao obter URI: ${e.message}", e)
            Toast.makeText(this, "Erro ao preparar arquivo (FileProvider).", Toast.LENGTH_LONG).show()
            null
        }

        if (fileUri != null) {
            Log.d("ShareBudget", "URI para compartilhar: $fileUri")
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // Verifica se há apps que podem lidar com a intent
            if (shareIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(shareIntent, "Compartilhar Orçamento Como Imagem"))
            } else {
                Log.e("ShareBudget", "Nenhum app para lidar com a intent de compartilhamento.")
                Toast.makeText(this, "Nenhum app de compartilhamento encontrado.", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e("ShareBudget", "fileUri é NULO. Não pode compartilhar.")
            Toast.makeText(this, "Não foi possível gerar o URI do arquivo para compartilhamento.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showApiError(errorBody: String?, defaultTitlePrefix: String) {
        // ... (função showApiError como antes) ...
        var errorMessage = defaultTitlePrefix
        if (!errorBody.isNullOrEmpty()) {
            errorMessage = try {
                val errorResponse = Gson().fromJson(errorBody, MessageResponse::class.java)
                "$defaultTitlePrefix: ${errorResponse.message}"
            } catch (parseEx: Exception) {
                "$defaultTitlePrefix: $errorBody (Falha ao parsear erro detalhado)"
            }
        }
        Toast.makeText(this@BudgetDetailActivity, errorMessage, Toast.LENGTH_LONG).show()
    }
}