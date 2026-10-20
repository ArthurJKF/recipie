package com.example.recipie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipie.data.model.*
import com.example.recipie.ui.components.*
import com.example.recipie.ui.viewmodel.RecipieViewModel
import java.time.LocalDate

@Composable
fun GeladeiraScreen(viewModel: RecipieViewModel) {
    val fridge by viewModel.fridge.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FridgeItem?>(null) }
    var filter by rememberSaveable { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { ScreenTitle("Minha geladeira", "Os ingredientes que você tem em casa.") }
        item { DatabaseError(viewModel) }
        item {
            Button(shape = MaterialTheme.shapes.small, onClick = { viewModel.clearActionError(); viewModel.searchIngredients(""); showAdd = true },
                modifier = Modifier.fillMaxWidth(), enabled = !busy) {
                Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text("Adicionar ingrediente")
            }
        }
        item { SearchField(filter, { filter = it }, "Buscar na minha geladeira") }
        item { Text("${fridge.size} ingredientes armazenados", style = MaterialTheme.typography.labelLarge) }
        if (fridge.isEmpty()) item {
            InfoCard("Sua geladeira está vazia", "Adicione ingredientes do catálogo ou crie os seus. Depois vá ao Fogão para encontrar receitas.")
        }
        val visible = fridge.filter { it.name.contains(filter, ignoreCase = true) }
        if (fridge.isNotEmpty() && visible.isEmpty()) item { Text("Nenhum ingrediente encontrado.") }
        items(visible, key = { it.entry.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text("${decimal(item.entry.quantityG)} g", style = MaterialTheme.typography.bodyMedium)
                            if (item.personalized) Text("Personalizado", color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium)
                            item.entry.expirationDate?.let { date ->
                                val expired = date < LocalDate.now().toString()
                                Text(if (expired) "Vencido em $date" else "Validade: $date",
                                    color = if (expired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton(onClick = { viewModel.clearActionError(); editing = item }, enabled = !busy) {
                            Icon(Icons.Outlined.Edit, "Alterar quantidade ou validade")
                        }
                        IconButton(onClick = { viewModel.removeFromFridge(item.entry.foodCode) }, enabled = !busy) {
                            Icon(Icons.Outlined.RemoveCircleOutline, "Retirar da geladeira")
                        }
                    }
                }
            }
        }
        item { Text("Ingredientes vencidos não entram na busca de receitas.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if (showAdd) IngredientDialog(viewModel, onDismiss = { showAdd = false })
    editing?.let { IngredientDialog(viewModel, initial = it, onDismiss = { editing = null }) }
}

@Composable
private fun IngredientDialog(viewModel: RecipieViewModel, initial: FridgeItem? = null, onDismiss: () -> Unit) {
    val query by viewModel.ingredientQuery.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val error by viewModel.actionError.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<CatalogIngredient?>(null) }
    var custom by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf(initial?.entry?.quantityG?.let(::decimal) ?: "100") }
    var expiration by rememberSaveable { mutableStateOf(initial?.entry?.expirationDate.orEmpty()) }
    var note by rememberSaveable { mutableStateOf("") }
    var removing by remember { mutableStateOf<CatalogIngredient?>(null) }
    val formVisible = initial != null || selected != null || custom
    FullScreenDialog(title = if (initial != null) "Editar ingrediente" else "Adicionar ingrediente",
        onDismiss = { if (!busy) onDismiss() }, bottomBar = {
            if (formVisible) SaveBar(viewModel, onSave = {
                when {
                    custom -> viewModel.addCustomIngredient(name, parseDecimal(quantity), expiration, note, onDismiss)
                    else -> viewModel.addToFridge(initial?.entry?.foodCode ?: selected!!.ingredient.ingredient_id,
                        parseDecimal(quantity), expiration, onDismiss)
                }
            })
        }) { padding ->
        if (formVisible) Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (custom) {
                OutlinedTextField(name, { name = it }, label = { Text("Nome do ingrediente") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text("Observação (opcional)") },
                    modifier = Modifier.fillMaxWidth())
            } else Text(initial?.name ?: selected?.ingredient?.ingredient_name.orEmpty(),
                style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(quantity, { quantity = it }, label = { Text("Quantidade em gramas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(expiration, { expiration = it }, label = { Text("Validade (opcional)") },
                supportingText = { Text("Formato AAAA-MM-DD, por exemplo 2027-03-25") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Text("Se o ingrediente já estiver na geladeira, a quantidade e a validade serão substituídas pelos valores acima.",
                style = MaterialTheme.typography.bodySmall)
            if (initial == null) TextButton(shape = MaterialTheme.shapes.small, onClick = { selected = null; custom = false }) { Text("Escolher outro ingrediente") }
        } else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { SearchField(query, viewModel::searchIngredients, "Buscar no catálogo") }
            item {
                OutlinedButton(shape = MaterialTheme.shapes.small, onClick = { viewModel.clearActionError(); custom = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text("Criar ingrediente personalizado")
                }
            }
            item { DatabaseError(viewModel) }
            item { Text("Busque pelo nome do alimento: arroz, leite, ovo… A busca também funciona sem acentos.", style = MaterialTheme.typography.bodySmall) }
            item { Text("Até 100 resultados. Refine a busca para encontrar mais.", style = MaterialTheme.typography.bodySmall) }
            if (catalog.isEmpty()) item { Text("Buscando… Se não aparecer um resultado, crie seu ingrediente.") }
            items(catalog, key = { it.ingredient.ingredient_id }) { item ->
                Card(onClick = { viewModel.clearActionError(); selected = item }) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(item.ingredient.ingredient_name.orEmpty(), style = MaterialTheme.typography.titleMedium)
                            if (item.personalized) Text("Personalizado", color = MaterialTheme.colorScheme.primary)
                            item.ingredient.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                        if (item.personalized) IconButton(onClick = { viewModel.clearActionError(); removing = item }, enabled = !busy) {
                            Icon(Icons.Outlined.DeleteOutline, "Excluir ingrediente personalizado")
                        } else Icon(Icons.Outlined.AddCircleOutline, "Adicionar")
                    }
                }
            }
        }
    }
    removing?.let { item ->
        ConfirmDelete("Excluir ingrediente?", "${item.ingredient.ingredient_name} será excluído do catálogo e da geladeira. Ingredientes usados em receitas precisam ser desvinculados primeiro.",
            busy, error, { removing = null; viewModel.clearActionError() },
            { viewModel.deleteCustomIngredient(item.ingredient.ingredient_id) { removing = null } })
    }
}
