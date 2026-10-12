package com.example.recipie

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipie.ui.viewmodel.RecipieViewModel

@Composable
fun FogaoScreen(viewModel: RecipieViewModel = viewModel()) {

    // Observa a string com o nome do ingrediente
    val nomeDoIngrediente by viewModel.nomeIngrediente.observeAsState(initial = "Carregando...")

    // Assim que a tela abrir, manda buscar o ingrediente de ID 1 (pode mudar o número)
    LaunchedEffect(Unit) {
        viewModel.testarBancoDeDados()
    }

    // Mostra o resultado no centro da tela
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nomeDoIngrediente,
            fontSize = 24.sp
        )
    }
}