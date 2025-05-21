package com.example.listmeapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listmeapp.data.model.LoginResponse
import com.example.listmeapp.auth.data.AuthRepository
import com.example.listmeapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginState = MutableStateFlow<Resource<LoginResponse>>(Resource.Loading())
    val loginState: StateFlow<Resource<LoginResponse>> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = repository.login(email, password)
            _loginState.value = result
        }
    }

    // Reset do estado para evitar flash de conteúdo ao voltar para a tela
    fun resetState() {
        _loginState.value = Resource.Loading()
    }
}