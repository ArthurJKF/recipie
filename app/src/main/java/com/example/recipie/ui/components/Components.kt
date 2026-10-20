package com.example.recipie.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipie.ui.viewmodel.RecipieViewModel
import java.util.Locale

@Composable
fun ScreenTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(value = value, onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        label = { Text(label) }, leadingIcon = { Icon(Icons.Outlined.Search, null) })
}

@Composable
fun InfoCard(title: String, message: String) {
    Surface(modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun DatabaseError(viewModel: RecipieViewModel) {
    val error by viewModel.databaseError.collectAsStateWithLifecycle()
    error?.let { ErrorText(it) }
}

@Composable
fun ErrorText(error: String) {
    Text(error, color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDialog(title: String, onDismiss: () -> Unit,
    bottomBar: @Composable () -> Unit = {}, content: @Composable (PaddingValues) -> Unit) {
    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(modifier = Modifier.safeDrawingPadding().imePadding(),
                topBar = { TopAppBar(title = { Text(title) }, navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Fechar") }
                }) }, bottomBar = bottomBar, content = content)
        }
    }
}

@Composable
fun SaveBar(viewModel: RecipieViewModel, onSave: () -> Unit, label: String = "Salvar") {
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val error by viewModel.actionError.collectAsStateWithLifecycle()
    Surface(shadowElevation = 0.dp, tonalElevation = 0.dp) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            error?.let { ErrorText(it) }
            Button(shape = MaterialTheme.shapes.small, onClick = onSave, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
                else Text(label)
            }
        }
    }
}

@Composable
fun ConfirmDelete(title: String, message: String, busy: Boolean, error: String?,
    onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(title) },
        text = { Column { Text(message); error?.let { ErrorText(it) } } },
        confirmButton = { TextButton(shape = MaterialTheme.shapes.small, onClick = onConfirm, enabled = !busy) { Text("Excluir") } },
        dismissButton = { TextButton(shape = MaterialTheme.shapes.small, onClick = onDismiss, enabled = !busy) { Text("Cancelar") } })
}

fun decimal(value: Double): String = if (value % 1.0 == 0.0) value.toLong().toString()
    else String.format(Locale.US, "%.1f", value)
fun parseDecimal(text: String): Double = text.trim().replace(',', '.').toDoubleOrNull() ?: Double.NaN
