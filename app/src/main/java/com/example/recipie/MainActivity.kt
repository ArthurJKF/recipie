package com.example.recipie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipie.ui.theme.ReciPieTheme
import com.example.recipie.ui.viewmodel.RecipieViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ReciPieTheme { RecipieApp() } }
    }
}

@Composable
fun RecipieApp(viewModel: RecipieViewModel = viewModel()) {
    val pager = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) { viewModel.messages.collect { snackbar.showSnackbar(it) } }
    BackHandler(enabled = pager.currentPage != 1) { scope.launch { pager.animateScrollToPage(1) } }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val titles = listOf("Geladeira", "Início", "Fogão")
                    titles.forEachIndexed { page, title ->
                        OutlinedButton(
                            onClick = { scope.launch { pager.animateScrollToPage(page) } },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                .semantics { selected = pager.currentPage == page },
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (pager.currentPage == page)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) { Text(title) }
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize().padding(padding),
            beyondViewportPageCount = 1) { page ->
            when (page) {
                0 -> GeladeiraScreen(viewModel)
                1 -> MainScreen()
                2 -> FogaoScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Recipie", style = MaterialTheme.typography.headlineMedium)
        HorizontalDivider()
        Text("Boas-vindas ao Recipie.")
        Text("Guarde ingredientes, procure receitas e salve suas favoritas.")
        HorizontalDivider()
        Text("Geladeira", style = MaterialTheme.typography.titleMedium)
        Text("Adicione os ingredientes que você tem em casa.")
        Text("Fogão", style = MaterialTheme.typography.titleMedium)
        Text("Busque receitas usando seus ingredientes.")
        HorizontalDivider()
        Text("Use os botões abaixo ou deslize para os lados.",
            style = MaterialTheme.typography.bodySmall)
    }
}
