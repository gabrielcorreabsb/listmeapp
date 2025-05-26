package com.example.listmeapp.auth.ui // Ou o pacote da sua Activity

import android.app.Activity // Import para Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult // Import para ActivityResult
import androidx.activity.result.contract.ActivityResultContracts // Import para o contrato
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.api.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetListActivity : AppCompatActivity() {

    private lateinit var rvBudgets: RecyclerView
    private lateinit var budgetListAdapter: BudgetListAdapter
    private lateinit var pbBudgetList: ProgressBar
    private lateinit var tvNoBudgets: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var fabCreateBudget: FloatingActionButton
    private var authToken: String? = null
    private var userCargo: String? = null // Para controle de permissões

    // Launcher para CreateBudgetActivity (tanto para novo quanto para edição se CreateBudgetActivity lidar com ambos)
    private val budgetFormLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Orçamento foi criado ou editado com sucesso
                Toast.makeText(this, "Lista de orçamentos será atualizada.", Toast.LENGTH_SHORT).show()
                fetchBudgets() // Recarrega a lista
            }
        }

    // Launcher para BudgetDetailActivity
    private val budgetDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Algo mudou na tela de detalhes (ex: orçamento deletado, status mudou)
                Toast.makeText(this, "Detalhes do orçamento alterados. Atualizando lista...", Toast.LENGTH_SHORT).show()
                fetchBudgets() // Recarrega a lista
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_list)

        toolbar = findViewById(R.id.toolbarBudgetList)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        rvBudgets = findViewById(R.id.rvBudgets)
        pbBudgetList = findViewById(R.id.pbBudgetList)
        tvNoBudgets = findViewById(R.id.tvNoBudgets)
        fabCreateBudget = findViewById(R.id.fabCreateBudget)

        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("AUTH_TOKEN", null)
        userCargo = sharedPreferences.getString("USER_CARGO", null)

        if (authToken == null) {
            Toast.makeText(this, "Erro de autenticação.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Vendedores e Admins podem criar orçamentos
        if (userCargo == "ADMIN" || userCargo == "VENDEDOR") {
            fabCreateBudget.visibility = View.VISIBLE // Mostra o FAB
            fabCreateBudget.setOnClickListener {
                val intent = Intent(this, CreateBudgetActivity::class.java)
                // Se CreateBudgetActivity também edita, você pode passar um extra aqui para indicar "novo"
                // intent.putExtra("IS_NEW_BUDGET", true)
                budgetFormLauncher.launch(intent) // Usa o launcher
            }
        } else {
            fabCreateBudget.visibility = View.GONE
        }

        setupRecyclerView()
        fetchBudgets()
    }

    private fun setupRecyclerView() {
        budgetListAdapter = BudgetListAdapter(emptyList()) { selectedBudget ->
            // Ação ao clicar em um orçamento: Abrir BudgetDetailActivity
            val intent = Intent(this, BudgetDetailActivity::class.java)
            intent.putExtra("BUDGET_ID", selectedBudget.id)
            // Você pode passar o objeto JSON aqui também se quiser evitar uma chamada de API na BudgetDetailActivity
            // intent.putExtra("BUDGET_OBJECT_JSON", Gson().toJson(selectedBudget))
            budgetDetailLauncher.launch(intent) // Usa o launcher para detalhes
        }
        rvBudgets.layoutManager = LinearLayoutManager(this)
        rvBudgets.adapter = budgetListAdapter
    }

    private fun fetchBudgets() {
        pbBudgetList.visibility = View.VISIBLE
        tvNoBudgets.visibility = View.GONE
        rvBudgets.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bearerToken = "Bearer $authToken"
                val response = RetrofitClient.orcamentoApi.getOrcamentos(bearerToken)
                withContext(Dispatchers.Main) {
                    pbBudgetList.visibility = View.GONE
                    if (response.isSuccessful) {
                        val budgets = response.body()
                        if (budgets.isNullOrEmpty()) {
                            tvNoBudgets.visibility = View.VISIBLE
                            rvBudgets.visibility = View.GONE
                        } else {
                            budgetListAdapter.updateBudgets(budgets)
                            tvNoBudgets.visibility = View.GONE
                            rvBudgets.visibility = View.VISIBLE
                        }
                    } else {
                        tvNoBudgets.visibility = View.VISIBLE
                        Log.e("FetchBudgetsError", "Código: ${response.code()}, Msg: ${response.errorBody()?.string()}")
                        Toast.makeText(this@BudgetListActivity, "Erro ao buscar orçamentos: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pbBudgetList.visibility = View.GONE
                    tvNoBudgets.visibility = View.VISIBLE
                    Log.e("FetchBudgetsExc", "Exceção: ${e.message}", e)
                    Toast.makeText(this@BudgetListActivity, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}