package com.example.listmeapp.auth.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment // Importe NavHostFragment
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

        // Maneira correta de obter o NavController de um NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home // ID do seu fragmento inicial no nav_graph
                // Adicione outros IDs de fragmentos de nível superior aqui, se houver
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)
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
        val ivNavLogo: ImageView = headerView.findViewById(R.id.ivNavHeaderLogo)

        tvNavUserName.text = userName
        tvNavUserEmail.text = userEmail
        ivNavLogo.setImageResource(R.drawable.ic_logo_listm) // Use seu logo
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
        // O NavController (via setupWithNavController) tentará navegar primeiro
        // se o ID do item de menu corresponder a um destino no gráfico de navegação.
        // Se não, ou se você quiser comportamento customizado (como iniciar uma Activity),
        // trate aqui e retorne true.

        when (item.itemId) {
            R.id.nav_home -> {
                navController.navigate(R.id.navigation_home) // Navega para o fragmento Home
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_clients -> {
                startActivity(Intent(this, ClientListActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_products -> {
                startActivity(Intent(this, ProductListActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_budgets -> {
                startActivity(Intent(this, BudgetListActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_users_admin -> {
                if (isAdmin) {
                    startActivity(Intent(this, UserListActivity::class.java))
                } else {
                    Toast.makeText(this, "Acesso restrito.", Toast.LENGTH_SHORT).show()
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Configurações (A implementar)", Toast.LENGTH_SHORT).show()
                // TODO: Navegar para um Fragment/Activity de Configurações
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_logout -> {
                performLogout()
                // Não precisa fechar a gaveta aqui, pois a activity será finalizada
                return true
            }
        }
        // Se nenhum item foi tratado acima, e setupWithNavController não tratou,
        // o comportamento padrão é não fazer nada ou deixar o sistema tratar.
        // Fechar a gaveta aqui também é uma boa prática se o item não foi tratado.
        drawerLayout.closeDrawer(GravityCompat.START)
        return false // Indica que o item não foi tratado por este listener (deixa o NavController tentar)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (!navController.popBackStack()) { // Tenta voltar na pilha do NavController
                super.onBackPressed() // Se não houver nada para voltar no NavController, fecha a Activity
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
                    RetrofitClient.instance.logout(bearerToken)
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