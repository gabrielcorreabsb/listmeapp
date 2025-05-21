package com.example.listmeapp.common

/**
 * Utilitários para validação de campos de formulário
 */
object ValidationUtils {
    /**
     * Verifica se um campo está vazio
     * @return mensagem de erro ou null se válido
     */
    fun validateNotEmpty(value: String, fieldName: String): String? {
        return if (value.isBlank()) {
            "$fieldName não pode ser vazio"
        } else {
            null
        }
    }

    /**
     * Verifica se um email é válido
     * @return mensagem de erro ou null se válido
     */
    fun validateEmail(email: String): String? {
        val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
        return if (email.isBlank()) {
            "Email não pode ser vazio"
        } else if (!email.matches(emailRegex)) {
            "Email inválido"
        } else {
            null
        }
    }

    /**
     * Verifica se uma senha atende aos requisitos mínimos
     * @return mensagem de erro ou null se válido
     */
    fun validatePassword(password: String): String? {
        return if (password.isBlank()) {
            "Senha não pode ser vazia"
        } else if (password.length < 6) {
            "Senha deve ter pelo menos 6 caracteres"
        } else {
            null
        }
    }
}