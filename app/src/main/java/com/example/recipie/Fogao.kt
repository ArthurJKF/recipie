package com.example.recipie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipie.data.model.RecipeMatch
import com.example.recipie.data.model.PortugueseText
import com.example.recipie.ui.components.*
import com.example.recipie.ui.viewmodel.RecipieViewModel

@Composable
fun FogaoScreen(viewModel: RecipieViewModel) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val loading by viewModel.loadingRecipes.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    var categoryMenu by remember { mutableStateOf(false) }
    var showCreate by rememberSaveable { mutableStateOf(false) }
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { ScreenTitle("Fogão", "Busque e consulte receitas.") }
        item { DatabaseError(viewModel) }
        item { SearchField(filters.query, { viewModel.setFilters(filters.copy(query = it)) }, "Buscar receita ou ingrediente") }
        item {
            Button(shape = MaterialTheme.shapes.small, onClick = { viewModel.clearActionError(); viewModel.resetDraft(); showCreate = true },
                modifier = Modifier.fillMaxWidth(), enabled = !busy) {
                Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text("Criar receita personalizada")
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(shape = MaterialTheme.shapes.small, selected = filters.useFridge,
                        onClick = { viewModel.setFilters(filters.copy(useFridge = !filters.useFridge,
                            completeOnly = if (filters.useFridge) false else filters.completeOnly)) },
                        label = { Text("Minha geladeira") },
                        leadingIcon = { Icon(Icons.Outlined.Kitchen, null, Modifier.size(18.dp)) })
                    FilterChip(shape = MaterialTheme.shapes.small, selected = filters.favoritesOnly,
                        onClick = { viewModel.setFilters(filters.copy(favoritesOnly = !filters.favoritesOnly)) },
                        label = { Text("Favoritos") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(shape = MaterialTheme.shapes.small, selected = filters.completeOnly,
                        onClick = { viewModel.setFilters(filters.copy(completeOnly = !filters.completeOnly,
                            useFridge = if (!filters.completeOnly) true else filters.useFridge)) },
                        label = { Text("Tenho todos") })
                    FilterChip(shape = MaterialTheme.shapes.small, selected = filters.customOnly,
                        onClick = { viewModel.setFilters(filters.copy(customOnly = !filters.customOnly)) },
                        label = { Text("Minhas receitas") })
                }
                Box {
                    OutlinedButton(shape = MaterialTheme.shapes.small, onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (filters.category.isEmpty()) "Todas as categorias" else filters.category,
                            modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ExpandMore, null)
                    }
                    DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                        DropdownMenuItem(text = { Text("Todas as categorias") }, onClick = {
                            viewModel.setFilters(filters.copy(category = "")); categoryMenu = false
                        })
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category) }, onClick = {
                                viewModel.setFilters(filters.copy(category = category)); categoryMenu = false
                            })
                        }
                    }
                }
                if (filters.useFridge) Text("A busca considera ingredientes presentes e dentro da validade. Confira as quantidades na receita.",
                    style = MaterialTheme.typography.bodySmall)
                Text("Até 100 resultados por busca. Use o nome e os filtros para refinar.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (!loading && recipes.isEmpty()) item {
            InfoCard("Nenhuma receita encontrada", if (filters.useFridge)
                "Adicione ingredientes à geladeira ou desative o filtro Minha geladeira para explorar a base. Confira também os outros filtros."
                else "Tente outro nome ou remova alguns filtros.")
        }
        items(recipes, key = { it.recipe.id }) { match ->
            RecipeCard(match, busy, onOpen = { detailId = match.recipe.id },
                onFavorite = { viewModel.toggleFavorite(match.recipe.id) })
        }
    }
    if (showCreate) RecipeEditor(viewModel) { showCreate = false }
    detailId?.let { id ->
        RecipeDetailDialog(viewModel, id) { detailId = null; viewModel.closeDetail() }
    }
}

@Composable
private fun RecipeCard(match: RecipeMatch, busy: Boolean, onOpen: () -> Unit, onFavorite: () -> Unit) {
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(match.recipe.category ?: "Receita", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Text(match.recipe.name ?: "Sem título", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = onFavorite, enabled = !busy) {
                    Icon(if (match.favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        if (match.favorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                match.recipe.total?.takeIf { it.isNotBlank() }?.let { Text("Tempo: ${PortugueseText.time(it)}", style = MaterialTheme.typography.bodySmall) }
                match.recipe.servings?.let { Text("${decimal(it)} porções", style = MaterialTheme.typography.bodySmall) }
            }
            if (match.requiredCount > 0) {
                val missing = match.requiredCount - match.availableCount
                Text(if (missing == 0) "Você tem todos os ingredientes" else "${match.availableCount}/${match.requiredCount} ingredientes · faltam $missing",
                    color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(progress = { match.availableCount.toFloat() / match.requiredCount },
                    modifier = Modifier.fillMaxWidth())
            } else Text("Confira os ingredientes nos detalhes.", style = MaterialTheme.typography.bodySmall)
            if (match.personalized) Text("Sua receita personalizada", style = MaterialTheme.typography.labelMedium)
            Text("Ver receita →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
