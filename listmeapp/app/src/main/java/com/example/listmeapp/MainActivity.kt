package com.example.listmeapp


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.listmeapp.ui.login.LoginScreen
import androidx.navigation.compose.rememberNavController
import com.example.listmeapp.ui.theme.ListMeTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onRegisterClick = {
                                    // Navegação para tela de cadastro (a ser implementada)
                                    Toast.makeText(this@MainActivity, "Cadastro em desenvolvimento", Toast.LENGTH_SHORT).show()
                                },
                                onForgotPasswordClick = {
                                    // Navegação para recuperação de senha (a ser implementada)
                                    Toast.makeText(this@MainActivity, "Recuperação de senha em desenvolvimento", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        composable("home") {
                            // Tela principal temporária
                            HomeScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Bem-vindo ao ListMe!",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}