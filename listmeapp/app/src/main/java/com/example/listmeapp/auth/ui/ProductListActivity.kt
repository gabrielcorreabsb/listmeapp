package com.example.listmeapp.auth.ui // Certifique-se que este é o pacote correto


import android.app.Activity // Import para Activity.RESULT_OK
import android.content.Context
import android.content.Intent // Import para Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.api.RetrofitClient
import com.example.listmeapp.data.model.ProdutoDTO
import com.example.listmeapp.data.model.UnidadeMedida
import com.example.listmeapp.data.model.MessageResponse
import com.example.listmeapp.auth.ui.ProductListAdapter // Ajuste o pacote do adapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
// Imports da Maskara não são necessários aqui, pois foram removidos do formulário de produto
// import com.santalu.maskara.Mask
// import com.santalu.maskara.MaskChangedListener
// import com.santalu.maskara.MaskStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.math.BigDecimal
// import java.text.DecimalFormatSymbols // Não mais necessário se normalizamos a string
import java.util.Locale

class ProductListActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter
    private lateinit var pbProductList: ProgressBar
    private lateinit var tvNoProducts: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var fabAddProduct: FloatingActionButton
    private var authToken: String? = null
    private var isAdmin: Boolean = false
    private var userCargo: String? = null // Adicionado para checar VENDEDOR também

    private var isSelectMode: Boolean = false // NOVA VARIÁVEL

    private val unidadeMedidaValues = UnidadeMedida.values()
    private val unidadeMedidaDisplayNames = unidadeMedidaValues.map { it.name.replace("_", " ").capitalizeWords() }

    // Listener para preço não é mais necessário se não usamos maskara para ele
    // private lateinit var priceListener: MaskChangedListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        toolbar = findViewById(R.id.toolbarProductList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            if (isSelectMode) {
                setResult(Activity.RESULT_CANCELED)
            }
            onBackPressedDispatcher.onBackPressed()
        }

        rvProducts = findViewById(R.id.rvProducts)
        pbProductList = findViewById(R.id.pbProductList)
        tvNoProducts = findViewById(R.id.tvNoProducts)
        fabAddProduct = findViewById(R.id.fabAddProduct)

        isSelectMode = intent.getBooleanExtra("SELECT_MODE", false)

        if (isSelectMode) {
            toolbar.title = "Selecionar Produto"
            fabAddProduct.visibility = View.GONE
        } else {
            toolbar.title = "Produtos"
        }

        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("AUTH_TOKEN", null)
        userCargo = sharedPreferences.getString("USER_CARGO", null) // Pega o cargo
        isAdmin = userCargo == "ADMIN"

        if (authToken == null) {
            Toast.makeText(this, "Erro de autenticação.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // FAB para adicionar produto só visível para Admin e se NÃO estiver em modo de seleção
        if (isAdmin && !isSelectMode) {
            fabAddProduct.visibility = View.VISIBLE
            fabAddProduct.setOnClickListener { showProductFormDialog(null) }
        } else {
            fabAddProduct.visibility = View.GONE
        }

        setupRecyclerView()
        fetchProducts()
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }

    private fun setupRecyclerView() {
        productListAdapter = ProductListAdapter(
            emptyList(),
            onItemClick = { product -> // Callback para clique no item inteiro
                if (isSelectMode) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("SELECTED_PRODUCT_JSON", Gson().toJson(product))
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    // Em modo normal, pode abrir detalhes ou não fazer nada no clique do item
                }
            },
            onEditClick = { product -> // Callback para o BOTÃO de editar
                // A visibilidade do botão de editar já é controlada pelo adapter e pelo isAdmin
                if (isAdmin && !isSelectMode) showProductFormDialog(product)
            },
            onDeleteClick = { product -> // Callback para o BOTÃO de deletar
                // A visibilidade do botão de deletar já é controlada pelo adapter e pelo isAdmin
                if (isAdmin && !isSelectMode) showDeleteConfirmationDialog(product)
            },
            isAdmin = isAdmin, // Admin pode editar/deletar (se não for select mode)
            isSelectMode = isSelectMode
        )
        rvProducts.adapter = productListAdapter
    }

    private fun fetchProducts() {
        pbProductList.visibility = View.VISIBLE
        tvNoProducts.visibility = View.GONE
        rvProducts.visibility = View.GONE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                // Vendedores e Admins podem listar. Se quiser apenas ativos para vendedores:
                val endpoint = if (userCargo == "VENDEDOR") "api/produtos/ativos" else "api/produtos"
                // Ou, mais simples, sempre buscar todos e filtrar na UI se necessário,
                // ou o backend já retorna o apropriado baseado no endpoint da API.
                // Vamos usar getProducts (que chama /api/produtos) por enquanto.
                val response = RetrofitClient.productApi.getProducts(bearerToken)
                withContext(Dispatchers.Main) {
                    pbProductList.visibility = View.GONE
                    if (response.isSuccessful) {
                        val products = response.body()
                        if (products.isNullOrEmpty()) {
                            tvNoProducts.visibility = View.VISIBLE
                            rvProducts.visibility = View.GONE
                        } else {
                            productListAdapter.updateProducts(products)
                            tvNoProducts.visibility = View.GONE
                            rvProducts.visibility = View.VISIBLE
                        }
                    } else {
                        tvNoProducts.visibility = View.VISIBLE
                        Log.e("FetchProdError", "Código: ${response.code()}, Mensagem: ${response.errorBody()?.string()}")
                        Toast.makeText(this@ProductListActivity, "Erro ao buscar produtos: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbProductList.visibility = View.GONE
                    tvNoProducts.visibility = View.VISIBLE
                    Log.e("FetchProdException", "Erro: ${e.message}", e)
                    Toast.makeText(this@ProductListActivity, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showProductFormDialog(productToEdit: ProdutoDTO?) {
        // ... (O código do showProductFormDialog permanece o mesmo da sua versão anterior) ...
        // ... (com a lógica de validação de nome, preço, e normalização de preço) ...
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product_form, null)
        val etProductName = dialogView.findViewById<TextInputEditText>(R.id.etDialogProductName)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDialogProductDescription)
        val etPrice = dialogView.findViewById<TextInputEditText>(R.id.etDialogProductPrice)
        val spinnerUnit = dialogView.findViewById<Spinner>(R.id.spinnerDialogProductUnit)
        val etImageUrl = dialogView.findViewById<TextInputEditText>(R.id.etDialogProductImageUrl)
        val switchActive = dialogView.findViewById<SwitchMaterial>(R.id.switchDialogProductActive)
        val pbDialog = dialogView.findViewById<ProgressBar>(R.id.pbDialogProductForm)

        etPrice.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unidadeMedidaDisplayNames)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = unitAdapter

        val dialogTitle: String
        if (productToEdit != null) {
            dialogTitle = "Editar Produto"
            etProductName.setText(productToEdit.nome)
            etDescription.setText(productToEdit.descricao)
            etPrice.setText(productToEdit.preco.toPlainString().replace(",", "."))
            productToEdit.unidadeMedida.let { currentUnitString ->
                val position = unidadeMedidaValues.indexOfFirst { it.name.equals(currentUnitString, ignoreCase = true) }
                if (position != -1) spinnerUnit.setSelection(position)
            }
            etImageUrl.setText(productToEdit.urlImagem)
            switchActive.isChecked = productToEdit.ativo ?: true
        } else {
            dialogTitle = "Adicionar Novo Produto"
            switchActive.isChecked = true
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(if (productToEdit != null) "Salvar" else "Adicionar", null)
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = etProductName.text.toString().trim()
                val description = etDescription.text.toString().trim()
                var priceStr = etPrice.text.toString().trim()
                val selectedUnitEnumName = unidadeMedidaValues[spinnerUnit.selectedItemPosition].name
                val imageUrl = etImageUrl.text.toString().trim()
                val active = switchActive.isChecked

                if (name.isEmpty()) {
                    etProductName.error = "Nome é obrigatório"
                    Toast.makeText(this, "Nome do produto é obrigatório.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    etProductName.error = null
                }

                if (priceStr.isEmpty()) {
                    etPrice.error = "Preço é obrigatório"
                    Toast.makeText(this, "Preço do produto é obrigatório.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                priceStr = priceStr.replace(',', '.')
                val price: BigDecimal
                try {
                    price = BigDecimal(priceStr)
                    if (price <= BigDecimal.ZERO) {
                        etPrice.error = "Preço deve ser um valor positivo"
                        return@setOnClickListener
                    }
                    etPrice.error = null
                } catch (e: NumberFormatException) {
                    etPrice.error = "Formato de preço inválido. Use '.' ou ',' como separador decimal."
                    return@setOnClickListener
                }

                pbDialog.visibility = View.VISIBLE
                positiveButton.isEnabled = false

                val productData = ProdutoDTO(
                    id = productToEdit?.id,
                    nome = name,
                    descricao = description.ifEmpty { null },
                    preco = price,
                    unidadeMedida = selectedUnitEnumName,
                    urlImagem = imageUrl.ifEmpty { null },
                    ativo = active
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val bearerToken = "Bearer $authToken"
                        val response: Response<ProdutoDTO> = if (productToEdit != null) {
                            RetrofitClient.productApi.updateProduct(bearerToken, productToEdit.id!!, productData)
                        } else {
                            RetrofitClient.productApi.createProduct(bearerToken, productData)
                        }

                        withContext(Dispatchers.Main) {
                            pbDialog.visibility = View.GONE
                            positiveButton.isEnabled = true
                            if (response.isSuccessful) {
                                val action = if (productToEdit != null) "atualizado" else "criado"
                                Toast.makeText(this@ProductListActivity, "Produto '${response.body()?.nome}' $action!", Toast.LENGTH_SHORT).show()
                                fetchProducts()
                                dialog.dismiss()
                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                                showApiError(errorMsg, "Falha ao ${if (productToEdit != null) "atualizar" else "criar"} produto")
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            pbDialog.visibility = View.GONE
                            positiveButton.isEnabled = true
                            Log.e("ProductFormExc", "Exceção: ${e.message}", e)
                            Toast.makeText(this@ProductListActivity, "Exceção: ${e.message}", Toast.LENGTH_LONG).show()
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
        Toast.makeText(this@ProductListActivity, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun showDeleteConfirmationDialog(product: ProdutoDTO) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir o produto ${product.nome}?")
            .setPositiveButton("Excluir") { _, _ ->
                product.id?.let { deleteProduct(it) }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteProduct(productId: Long) {
        pbProductList.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.productApi.deleteProduct(bearerToken, productId)
                withContext(Dispatchers.Main) {
                    pbProductList.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductListActivity, "Produto excluído!", Toast.LENGTH_SHORT).show()
                        fetchProducts()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Erro ${response.code()}"
                        showApiError(errorMsg, "Falha ao excluir produto")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbProductList.visibility = View.GONE
                    Log.e("DeleteProdExc", "Exceção: ${e.message}", e)
                    Toast.makeText(this@ProductListActivity, "Exceção ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}