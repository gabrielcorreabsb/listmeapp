package com.example.listmeapp.common

sealed class Resource<T>(
    open val data: T? = null,
    open val message: String? = null
) {
    class Idle<T> : Resource<T>() // Para estado inicial ou ocioso
    class Loading<T>(data: T? = null) : Resource<T>(data = data) // Loading pode ter dados antigos
    class Success<T>(data: T?) : Resource<T>(data = data) // Success pode ter data nula (ex: logout bem-sucedido sem corpo de dados)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data = data, message = message)
}