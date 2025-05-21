package com.example.listmeapp.auth.ui // Ou o pacote correto onde está sua UI de login

import android.app.Application
import android.content.Intent // Para navegação de exemplo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // Para a Factory do ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.listmeapp.MainActivity // Supondo que você tenha uma MainActivity para ir após o login
import com.example.listmeapp.common.Resource
import com.example.listmeapp.ui.theme.ListMeTheme // Importe seu tema

// ViewModelFactory simples se LoginViewModel for AndroidViewModel e não usar Hilt
class LoginViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListMeTheme { // Seu tema aqui
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Fornecendo o ViewModel com a Factory se ele for AndroidViewModel
                    val factory = LoginViewModelFactory(application)
                    val loginViewModel: LoginViewModel = viewModel(factory = factory)

                    if (loginViewModel.isUserAlreadyLoggedIn()) {
                        LaunchedEffect(Unit) {
                            Toast.makeText(this@LoginActivity, "Usuário já logado! Navegando para Home...", Toast.LENGTH_SHORT).show()
                            // Exemplo de navegação para uma MainActivity (ou sua tela principal)
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish() // Fecha a LoginActivity
                        }
                    } else {
                        // Passando o ViewModel para LoginScreen
                        LoginScreen(
                            loginViewModel = loginViewModel,
                            onLoginSuccess = {
                                // Ação de navegação após sucesso no login
                                Toast.makeText(this@LoginActivity, "Navegando para Home...", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel, // ViewModel agora é um parâmetro obrigatório
    onLoginSuccess: () -> Unit // Callback para sucesso
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Usar 'by' para delegação
    val loginStateValue: Resource<out Any?> by loginViewModel.loginState.collectAsState() // Usar 'out Any?' para o tipo genérico se necessário, ou o tipo específico.
    // No seu caso, é Resource<LoginResponse?>
    val context = LocalContext.current

    LaunchedEffect(key1 = loginStateValue) { // Usar loginStateValue como chave
        when (val state = loginStateValue) {
            is Resource.Success -> {
                if (state.data != null) { // Sucesso no login com dados
                    // O Toast é bom para debug, mas a navegação é o principal
                    // Toast.makeText(context, "Login bem-sucedido! Bem-vindo", Toast.LENGTH_LONG).show()
                    onLoginSuccess() // Chama o callback para navegar
                    // O reset do estado deve acontecer após a navegação ou tratamento
                    // loginViewModel.resetState() // O ViewModel pode resetar internamente ou a Activity/Navegador lida com o ciclo de vida
                }
            }
            is Resource.Error -> {
                state.message?.let {
                    Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show()
                }
                // Não resetar o estado aqui automaticamente, para que o usuário veja o erro
                // O usuário pode tentar novamente ou o ViewModel pode ter lógica para resetar
            }
            is Resource.Loading -> {
                // UI já mostra o indicador de loading
            }
            is Resource.Idle -> { // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ADICIONADO
                // Estado inicial ou ocioso, não faz nada explicitamente aqui
                // A UI simplesmente mostrará os campos vazios.
            }
            // Se você não quiser um 'else', todos os casos da sealed class devem ser cobertos.
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Login ListMe", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuário (login)") }, // Ajustado para clareza
                modifier = Modifier.fillMaxWidth(),
                isError = loginStateValue is Resource.Error // Exemplo de destacar campo em erro
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = loginStateValue is Resource.Error // Exemplo
            )

            // Exibe a mensagem de erro do ViewModel se houver
            if (loginStateValue is Resource.Error) {
                (loginStateValue as Resource.Error<*>).message?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }


            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        // O ViewModel deve ser chamado com 'login' (ou o que seu backend espera)
                        loginViewModel.login(usernameInput = username, passwordInput = password)
                    } else {
                        Toast.makeText(context, "Preencha usuário e senha.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = loginStateValue !is Resource.Loading
            ) {
                Text("Entrar")
            }

            if (loginStateValue is Resource.Loading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ListMeTheme {
        // Para o preview, você precisa de um Application mock ou real, e um callback.
        // A forma mais simples de fazer o preview funcionar é se o ViewModel não precisar de Application.
        // Se precisar, o preview pode ficar mais complexo.
        val mockApplication = LocalContext.current.applicationContext as Application
        LoginScreen(
            loginViewModel = LoginViewModel(mockApplication), // Ou um ViewModel mockado
            onLoginSuccess = {}
        )
    }
}