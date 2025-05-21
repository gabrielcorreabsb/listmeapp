package com.example.listmeapp.auth.ui // Ou com.example.listmeapp.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel // Para ter acesso ao Application Context
import androidx.lifecycle.viewModelScope
import com.example.listmeapp.auth.data.AuthRepository
import com.example.listmeapp.auth.data.TokenManager
import com.example.listmeapp.common.Resource
import com.example.listmeapp.data.model.LoginResponse
// import com.example.listmeapp.data.model.MessageResponse // Não usado diretamente no estado público aqui
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository = AuthRepository() // Sem DI
    private val tokenManager: TokenManager = TokenManager(application.applicationContext) // Sem DI

    // O tipo genérico de Resource é LoginResponse? para permitir data nula
    private val _loginState = MutableStateFlow<Resource<LoginResponse?>>(Resource.Idle())
    val loginState: StateFlow<Resource<LoginResponse?>> = _loginState

    // login (nome do método) e o campo do LoginRequest (login)
    // Se seu backend espera "email", o parâmetro deve ser email ou mapeado.
    // Vou usar "username" para ser genérico, mas seu backend espera "login".
    fun login(usernameInput: String, passwordInput: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            // repository.login retorna Resource<LoginResponse?>
            val result = repository.login(username = usernameInput, passwordValue = passwordInput)
            if (result is Resource.Success && result.data != null) {
                tokenManager.saveToken(result.data.token, result.data.type)
                // Não emitir o result diretamente aqui se você vai navegar.
                // A navegação deve acontecer na UI baseada neste sucesso.
                // O _loginState.value = result abaixo já notificará a UI.
            }
            _loginState.value = result
        }
    }

    // Chamado pela UI para resetar o estado (ex: após navegação ou tratamento de erro)
    fun resetState() {
        _loginState.value = Resource.Idle()
    }

    fun isUserAlreadyLoggedIn(): Boolean {
        return tokenManager.isUserLoggedIn()
    }

    // Você também pode adicionar uma função de logout aqui se necessário
    // fun performLogout() { ... chama repository.logout() e tokenManager.clearToken() ... }
}