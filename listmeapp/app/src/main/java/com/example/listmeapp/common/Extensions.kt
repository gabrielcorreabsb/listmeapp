package com.example.listmeapp.common

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

/**
 * Extensão para mostrar Toast de forma mais concisa
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Composable para coletar um Flow e executar uma ação para cada valor emitido
 */
@Composable
fun <T> CollectEffect(
    flow: Flow<T>,
    onCollect: (T) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = flow) {
        flow.collect { value ->
            onCollect(value)
        }
    }
}