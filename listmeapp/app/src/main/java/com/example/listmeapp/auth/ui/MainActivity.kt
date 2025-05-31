package com.example.listmeapp.auth.ui // Ou o pacote onde sua MainActivity está

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem // Essencial para onNavigationItemSelected
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Use androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.listmeapp.R
import com.example.listmeapp.data.api.RetrofitClient
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar

    private var userCargo: String? = null
    private var isAdmin: Boolean = false
    private var authToken: String? = null
    private var userName: String? = null
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        // Inicializar o NavController de forma segura
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment
        if (navHostFragment == null) {
            Log.e("MainActivity", "NavHostFragment não encontrado com ID R.id.nav_host_fragment_content_main")
            Toast.makeText(this, "Erro: NavHostFragment não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        navController = navHostFragment.navController

        // Configurar AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home), drawerLayout
        )

        // Configurar ActionBar com NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Conectar NavigationView com NavController
        navigationView.setupWithNavController(navController)

        // Definir listener para itens do menu
        navigationView.setNavigationItemSelectedListener(this)

        loadUserData()

        if (authToken == null) {
            Toast.makeText(this, "Sessão inválida. Por favor, faça login.", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        setupNavHeader()
        setupMenuVisibility()
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        authToken = sharedPreferences.getString("AUTH_TOKEN", null)
        userName = sharedPreferences.getString("USER_NAME", "Usuário ListMe")
        userEmail = sharedPreferences.getString("USER_EMAIL", "email@listme.app")
        userCargo = sharedPreferences.getString("USER_CARGO", null)
        isAdmin = userCargo == "ADMIN"
    }

    private fun setupNavHeader() {
        val headerView: View = navigationView.getHeaderView(0)
        val tvNavUserName: TextView = headerView.findViewById(R.id.tvNavHeaderUserName)
        val tvNavUserEmail: TextView = headerView.findViewById(R.id.tvNavHeaderUserEmail)
        val ivNavLogo: ImageView = headerView.findViewById(R.id.ivNavHeaderLogo) // Se tiver

        tvNavUserName.text = userName
        tvNavUserEmail.text = userEmail
        ivNavLogo.setImageResource(R.drawable.ic_logo_listm) // Exemplo
    }

    private fun setupMenuVisibility() {
        val menu = navigationView.menu
        val adminUsersMenuItem = menu.findItem(R.id.nav_users_admin)
        val clientsMenuItem = menu.findItem(R.id.nav_clients)
        val productsMenuItem = menu.findItem(R.id.nav_products)
        val budgetsMenuItem = menu.findItem(R.id.nav_budgets)

        adminUsersMenuItem?.isVisible = isAdmin

        val canManageClientProductBudget = isAdmin || userCargo == "VENDEDOR"
        clientsMenuItem?.isVisible = canManageClientProductBudget
        productsMenuItem?.isVisible = canManageClientProductBudget
        budgetsMenuItem?.isVisible = canManageClientProductBudget
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var handled = false
        when (item.itemId) {
            R.id.nav_home -> {
                // Se este item está no appBarConfiguration e setupWithNavController foi usado,
                // o NavController deve lidar com a navegação para o fragment.
                // Se precisar forçar ou tiver lógica extra:
                // navController.navigate(R.id.navigation_home)
                // handled = true // O NavController geralmente retorna true se navegou.
            }
            R.id.nav_clients -> {
                startActivity(Intent(this, ClientListActivity::class.java))
                handled = true
            }
            R.id.nav_products -> {
                startActivity(Intent(this, ProductListActivity::class.java))
                handled = true
            }
            R.id.nav_budgets -> {
                startActivity(Intent(this, BudgetListActivity::class.java))
                handled = true
            }
            R.id.nav_users_admin -> {
                if (isAdmin) {
                    startActivity(Intent(this, UserListActivity::class.java))
                    handled = true
                } else {
                    Toast.makeText(this, "Acesso restrito.", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Configurações (A implementar)", Toast.LENGTH_SHORT).show()
                handled = true
            }
            R.id.nav_logout -> {
                performLogout()
                handled = true // O logout já lida com o finish/redirect
            }
            else -> {
                // Se não foi um dos seus itens customizados, deixe o NavController tentar (se houver correspondência de ID)
                // Esta parte é mais para quando setupWithNavController não faz tudo sozinho.
                // Com setupWithNavController, a navegação para fragments com IDs correspondentes é automática.
                // handled = NavigationUI.onNavDestinationSelected(item, navController)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return handled // Retorne true se você tratou o clique.
        // Se o item do menu corresponde a um destino no NavController e
        // setupWithNavController está ativo, ele já deve ter navegado, e
        // retornar true aqui está correto.
        // Se você não tratou, pode retornar o resultado de onNavDestinationSelected
        // ou false para permitir que outros listeners (se houver) processem.
    }

    override fun onSupportNavigateUp(): Boolean {
        // Lida com o ícone de hamburger/seta "up" na Toolbar
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Deixa o NavController lidar com o botão voltar para fragments.
            // Se não houver mais nada na backstack do NavController, super.onBackPressed() fecha a Activity.
            if (!navController.popBackStack()) {
                super.onBackPressed()
            }
        }
    }

    private fun performLogout() {
        Log.d("MainActivity", "Logout button clicked")
        val sharedPreferences = getSharedPreferences("ListMeAppPrefs", Context.MODE_PRIVATE)
        val tokenToClear = sharedPreferences.getString("AUTH_TOKEN", null)

        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        Log.i("Auth", "SharedPreferences limpas.")

        if (tokenToClear != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bearerToken = "Bearer $tokenToClear"
                    RetrofitClient.instance.logout(bearerToken) // Assumindo que 'instance' é sua AuthApi
                } catch (e: Exception) {
                    Log.e("LogoutAPI", "Exceção ao chamar API de logout: ${e.message}")
                } finally {
                    withContext(Dispatchers.Main) {
                        redirectToLogin()
                    }
                }
            }
        } else {
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}