package com.example.recipie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipie.data.model.RecipeIngredientDraft
import com.example.recipie.ui.components.*
import com.example.recipie.ui.viewmodel.RecipieViewModel

@Composable
fun RecipeEditor(viewModel: RecipieViewModel, onDismiss: () -> Unit) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    var choosingIngredient by remember { mutableStateOf(false) }
    FullScreenDialog(title = "Nova receita", onDismiss = { if (!busy) onDismiss() },
        bottomBar = { SaveBar(viewModel, { viewModel.saveRecipe(onDismiss) }, "Salvar receita") }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Dados da receita", style = MaterialTheme.typography.headlineSmall) }
            item { OutlinedTextField(draft.name, { viewModel.updateDraft(draft.copy(name = it)) },
                label = { Text("Nome da receita *") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(draft.category, { viewModel.updateDraft(draft.copy(category = it)) },
                label = { Text("Categoria") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(draft.summary, { viewModel.updateDraft(draft.copy(summary = it)) },
                label = { Text("Descrição (opcional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(draft.prepMinutes, { viewModel.updateDraft(draft.copy(prepMinutes = it)) },
                        label = { Text("Preparo (min)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(draft.cookMinutes, { viewModel.updateDraft(draft.copy(cookMinutes = it)) },
                        label = { Text("Cozimento (min)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true)
                }
            }
            item { OutlinedTextField(draft.servings, { viewModel.updateDraft(draft.copy(servings = it.replace(',', '.'))) },
                label = { Text("Porções (opcional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item {
                Text("Ingredientes *", style = MaterialTheme.typography.titleLarge)
                Text("Selecione no catálogo. Para criar um ingrediente novo, use a Geladeira antes de montar a receita.",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedButton(shape = MaterialTheme.shapes.small, onClick = { viewModel.searchIngredients(""); choosingIngredient = true },
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text("Selecionar ingrediente")
                }
            }
            items(draft.ingredients, key = { it.ingredientId }) { ingredient ->
                fun update(item: RecipeIngredientDraft) {
                    viewModel.updateDraft(draft.copy(ingredients = draft.ingredients.map {
                        if (it.ingredientId == item.ingredientId) item else it
                    }))
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row {
                            Text(ingredient.name, style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                viewModel.updateDraft(draft.copy(ingredients = draft.ingredients.filterNot { it.ingredientId == ingredient.ingredientId }))
                            }) { Icon(Icons.Outlined.Close, "Remover ingrediente da receita") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(ingredient.quantity, { update(ingredient.copy(quantity = it.replace(',', '.'))) },
                                label = { Text("Quantidade") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(ingredient.unit, { update(ingredient.copy(unit = it)) },
                                label = { Text("Unidade") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                        OutlinedTextField(ingredient.note, { update(ingredient.copy(note = it)) },
                            label = { Text("Ex.: picado, sem casca (opcional)") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            item { OutlinedTextField(draft.instructions, { viewModel.updateDraft(draft.copy(instructions = it)) },
                label = { Text("Modo de preparo *") }, minLines = 5, modifier = Modifier.fillMaxWidth()) }
            item { Text("* Campos obrigatórios. Sua receita ficará disponível no Fogão.", style = MaterialTheme.typography.bodySmall) }
        }
    }
    if (choosingIngredient) RecipeIngredientPicker(viewModel,
        selected = draft.ingredients.map { it.ingredientId }.toSet(),
        onDismiss = { choosingIngredient = false }, onSelect = { id, name ->
            viewModel.updateDraft(draft.copy(ingredients = draft.ingredients + RecipeIngredientDraft(id, name, "1", "unidade")))
            choosingIngredient = false
        })
}

@Composable
private fun RecipeIngredientPicker(viewModel: RecipieViewModel, selected: Set<Long>,
    onDismiss: () -> Unit, onSelect: (Long, String) -> Unit) {
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val query by viewModel.ingredientQuery.collectAsStateWithLifecycle()
    FullScreenDialog("Selecionar ingrediente", onDismiss) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { SearchField(query, viewModel::searchIngredients, "Buscar ingrediente") }
            item { Text("Até 100 resultados. Você também pode selecionar ingredientes que não estão na geladeira.",
                style = MaterialTheme.typography.bodySmall) }
            val visible = catalog.filterNot { it.ingredient.ingredient_id in selected }
            if (visible.isEmpty()) item { Text("Nenhum ingrediente disponível nesta busca.") }
            items(visible, key = { it.ingredient.ingredient_id }) { ingredient ->
                Card(onClick = { onSelect(ingredient.ingredient.ingredient_id, ingredient.ingredient.ingredient_name.orEmpty()) },
                    modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        Text(ingredient.ingredient.ingredient_name.orEmpty(), modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.Add, "Selecionar")
                    }
                }
            }
        }
    }
}
