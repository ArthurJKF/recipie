package com.example.recipie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipie.ui.components.*
import com.example.recipie.ui.viewmodel.RecipieViewModel

@Composable
fun RecipeDetailDialog(viewModel: RecipieViewModel, id: Long, onDismiss: () -> Unit) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val translating by viewModel.translating.collectAsStateWithLifecycle()
    val translationError by viewModel.translationError.collectAsStateWithLifecycle()
    val loading by viewModel.loadingDetail.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val error by viewModel.actionError.collectAsStateWithLifecycle()
    var deleting by remember { mutableStateOf(false) }
    LaunchedEffect(id) { viewModel.openDetail(id) }
    FullScreenDialog(title = "Detalhes da receita", onDismiss = onDismiss) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            val data = detail
            if (!loading && data == null) item { Text("Não foi possível carregar a receita.") }
            if (data != null) {
                val recipe = data.recipe
                item {
                    Text(recipe.category.orEmpty(), color = MaterialTheme.colorScheme.primary)
                    Text(recipe.name.orEmpty(), style = MaterialTheme.typography.headlineMedium)
                    recipe.author?.takeIf { it.isNotBlank() }?.let { Text("Por $it", style = MaterialTheme.typography.bodySmall) }
                }
                item {
                    OutlinedButton(shape = MaterialTheme.shapes.small, onClick = { viewModel.toggleFavorite(id) }, enabled = !busy,
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(if (data.favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (data.favorite) "Remover dos favoritos" else "Adicionar aos favoritos")
                    }
                }
                recipe.summary?.takeIf { it.isNotBlank() }?.let { summary -> item { Text(summary) } }
                item {
                    InfoCard("Preparo e rendimento", listOfNotNull(
                        recipe.prep?.let { "Preparo: $it" }, recipe.cook?.let { "Cozimento: $it" },
                        recipe.total?.let { "Tempo total: $it" }, recipe.servings?.let { "Porções: ${decimal(it)}" },
                        recipe.rating?.let { "Avaliação: ${decimal(it)}/5" }).joinToString("\n").ifEmpty { "Não informado." })
                }
                if (translating) item {
                    InfoCard("Preparando receita em português", "Na primeira vez, o celular baixa a tradução pelo Wi-Fi. Aguarde; os nomes dos ingredientes já estão disponíveis abaixo.")
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                translationError?.let { message -> item {
                    ErrorText(message)
                    OutlinedButton(shape = MaterialTheme.shapes.small, onClick = { viewModel.openDetail(id) }) { Text("Tentar traduzir novamente") }
                } }
                item { Text("Ingredientes", style = MaterialTheme.typography.titleLarge) }
                if (!recipe.ingredients.isNullOrBlank()) item {
                    Text(recipe.ingredients.split(';', '\n').map { it.trim() }.filter { it.isNotEmpty() }
                        .joinToString("\n") { "• $it" })
                }
                if (data.ingredients.isNotEmpty()) {
                    item { Text("Disponibilidade na sua geladeira", style = MaterialTheme.typography.titleMedium) }
                    items(data.ingredients) { ingredient ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(if (ingredient.available) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                if (ingredient.available) "Na geladeira" else "Falta",
                                tint = if (ingredient.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp))
                            Column {
                                Text(ingredient.name)
                                Text(if (ingredient.available) "Na geladeira" else "Falta na geladeira", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    item { Text("A disponibilidade indica a presença do ingrediente. Confira a quantidade e a unidade na lista acima.",
                        style = MaterialTheme.typography.bodySmall) }
                }
                item { HorizontalDivider(); Text("Modo de preparo", style = MaterialTheme.typography.titleLarge) }
                item { Text(recipe.instructions?.takeIf { it.isNotBlank() }
                    ?: if (recipe.originalName != null && recipe.instructionsPt == null) "O modo de preparo aparecerá aqui após a tradução." else "Não informado.") }
                if (recipe.originalName != null && recipe.instructions != null) item {
                    Text(if (id == 171L) "Texto revisado em português. Medidas aproximadas no sistema métrico."
                        else "Tradução automática para português. Conversões de medidas são aproximadas.",
                        style = MaterialTheme.typography.bodySmall)
                }
                if (recipe.calories != null || recipe.protein_g != null || recipe.carbohydrates_g != null) item {
                    InfoCard("Informação nutricional da base", listOfNotNull(
                        recipe.calories?.let { "Energia: ${decimal(it)} kcal" },
                        recipe.protein_g?.let { "Proteínas: ${decimal(it)} g" },
                        recipe.carbohydrates_g?.let { "Carboidratos: ${decimal(it)} g" },
                        recipe.fat_g?.let { "Gorduras: ${decimal(it)} g" }).joinToString("\n"))
                }
                if (data.personalized) item {
                    OutlinedButton(shape = MaterialTheme.shapes.small, onClick = { viewModel.clearActionError(); deleting = true },
                        enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.DeleteOutline, null); Spacer(Modifier.width(8.dp)); Text("Excluir receita personalizada")
                    }
                }
            }
        }
    }
    if (deleting) ConfirmDelete("Excluir receita?", "A receita personalizada e seu vínculo com os favoritos serão removidos.",
        busy, error, { deleting = false; viewModel.clearActionError() },
        { viewModel.deleteRecipe(id) { deleting = false; onDismiss() } })
}
