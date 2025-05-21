package com.example.listmeapp.ui.login // Ou com.example.listmeapp.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
// Imports do Compose Runtime Corretos:
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State // Import State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // Import getValue para delegação 'by'
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue // Import setValue para delegação 'by' em var
// Fim dos imports do Compose Runtime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color // Import genérico de Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Para viewModel()
import com.example.listmeapp.common.Resource
import com.example.listmeapp.data.model.LoginResponse
// Assumindo que seus componentes e tema estão nesses pacotes:
import com.example.listmeapp.ui.components.StandardButton
import com.example.listmeapp.ui.components.StandardTextField
import com.example.listmeapp.ui.theme.* // Para suas cores e tema

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: LoginViewModel = viewModel() // Se ViewModel não precisar de Application no construtor
    // Se LoginViewModel for AndroidViewModel, você precisa de uma Factory ou Hilt
    // viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    // Tipo explícito para ajudar o compilador e garantir que 'by' funcione
    val loginStateValue: Resource<LoginResponse?> by viewModel.loginState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var usernameInput by remember { mutableStateOf("") } // Renomeado para clareza
    var passwordInput by remember { mutableStateOf("") } // Renomeado para clareza

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = loginStateValue) { // Reage a mudanças no estado
        when (val state = loginStateValue) {
            is Resource.Success -> {
                if (state.data != null) { // Login bem-sucedido com dados
                    Toast.makeText(context, "Login: ${state.data.user.nome}", Toast.LENGTH_LONG).show()
                    onLoginSuccess()
                    // O ViewModel deve resetar o estado APÓS a navegação ou tratamento
                    // viewModel.resetState() // Movido para após onLoginSuccess ser chamado
                }
                // Se data for null, pode ser um estado de Success de logout ou Idle resetado.
            }
            is Resource.Error -> {
                // A mensagem de erro já é exibida pela UI abaixo.
                // Não é necessário um Toast aqui, a menos que desejado.
            }
            is Resource.Loading -> {
                // UI já mostra o indicador de loading.
            }
            is Resource.Idle -> {
                // Estado inicial ou resetado, não faz nada.
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray) // Use suas cores de tema
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp) // Padding horizontal para a coluna inteira
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // Aumentado um pouco
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight() // Para centralizar verticalmente dentro do Box do Header
                        .background(TealPrimary) // Mover o background para cá
                        .padding(16.dp) // Padding interno para o conteúdo do header
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(40.dp),
                        color = White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "LM",
                                color = TealPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ListMe",
                        color = White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    // .padding(horizontal = 16.dp) // Removido, já que a Column pai tem
                    .offset(y = (-50).dp), // Aumentar sobreposição
                shape = RoundedCornerShape(16.dp), // Cantos mais arredondados
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Acesse sua conta",
                        style = MaterialTheme.typography.headlineSmall, // Usar estilos do tema
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mensagem de erro do ViewModel
                    if (loginStateValue is Resource.Error) {
                        val errorMessage = (loginStateValue as Resource.Error<LoginResponse?>).message
                        if (!errorMessage.isNullOrEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                                    .padding(bottom = 16.dp) // Espaço abaixo do erro
                            )
                        }
                    }

                    StandardTextField(
                        value = usernameInput,
                        onValueChange = {
                            usernameInput = it
                            usernameError = null
                        },
                        label = "Login (usuário ou e-mail)", // Ajustado para clareza
                        isError = usernameError != null,
                        errorMessage = usernameError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text, // Pode ser Email ou Text
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StandardTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = null
                        },
                        label = "Senha",
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                var isValid = true
                                if (usernameInput.isBlank()) {
                                    usernameError = "Login não pode ser vazio"
                                    isValid = false
                                }
                                if (passwordInput.isBlank()) {
                                    passwordError = "Senha não pode ser vazia"
                                    isValid = false
                                }
                                if (isValid) {
                                    viewModel.login(usernameInput, passwordInput)
                                }
                            }
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(onClick = onForgotPasswordClick) {
                            Text("Esqueceu a senha?", color = TealPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    StandardButton(
                        text = "Entrar",
                        onClick = {
                            var isValid = true
                            if (usernameInput.isBlank()) {
                                usernameError = "Login não pode ser vazio"
                                isValid = false
                            }
                            if (passwordInput.isBlank()) {
                                passwordError = "Senha não pode ser vazia"
                                isValid = false
                            }
                            if (isValid) {
                                focusManager.clearFocus()
                                viewModel.login(usernameInput, passwordInput)
                            }
                        },
                        isLoading = loginStateValue is Resource.Loading,
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Empurra para baixo

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Não tem uma conta?", color = TextSecondary)
                TextButton(onClick = onRegisterClick) {
                    Text("Cadastre-se", color = TealPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ViewModelFactory simples se LoginViewModel for AndroidViewModel e não usar Hilt
// class LoginViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return LoginViewModel(application) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
// }


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LoginScreenPreview() {
    // Certifique-se que ListMeTheme está definido e suas cores são acessíveis
    ListMeTheme {
        LoginScreen(
            onLoginSuccess = {},
            onRegisterClick = {},
            onForgotPasswordClick = {}
            // viewModel = viewModel() // O Preview usará um ViewModel padrão se não especificado
        )
    }
}

// Definições de cores de exemplo (coloque-as em seu arquivo Theme.kt ou Color.kt)
// Se você já as tem, não precisa adicionar estas.
// val BackgroundGray = Color(0xFFF0F2F5)
// val TealPrimary = Color(0xFF00796B) // Exemplo
// val White = Color.White
// val TextPrimary = Color.Black
// val ErrorRed = Color(0xFFB00020) // Cor de erro do Material Design
// val TextSecondary = Color.Gray