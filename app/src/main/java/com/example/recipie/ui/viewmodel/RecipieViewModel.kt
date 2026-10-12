package com.example.recipie.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.recipie.data.local.database.RecipieDatabaseProvider
import com.example.recipie.data.repository.RecipieRepository
import kotlinx.coroutines.launch

class RecipieViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipieRepository

    // Variável que vai guardar APENAS o nome do ingrediente
    private val _nomeIngrediente = MutableLiveData<String>("Buscando...")
    val nomeIngrediente: LiveData<String> = _nomeIngrediente

    init {
        val database = RecipieDatabaseProvider.getDatabase(getApplication())
        repository = RecipieRepository(
            database.recipeDao(),
            database.ingredienteDao(),
            database.recipeIngredientDao()
        )
    }
    fun testarBancoDeDados() {
        viewModelScope.launch {
            try {
                // Traz todos os ingredientes do banco
                val todos = repository.getAllIngredientes()

                if (todos.isEmpty()) {
                    _nomeIngrediente.value = "O banco está VAZIO (0 ingredientes)."
                } else {
                    val primeiro = todos.first()
                    _nomeIngrediente.value = "Sucesso! Total: ${todos.size} ingredientes.\nO primeiro é '${primeiro.ingredient_name}' com ID ${primeiro.ingredient_id}"
                }
            } catch (e: Exception) {
                _nomeIngrediente.value = "Erro ao ler: ${e.message}"
            }
        }
    }
    // Função que busca pelo ID e atualiza o texto
    fun buscarNomeDoIngrediente(id: Long) {
        viewModelScope.launch {
            try {
                val ingrediente = repository.getIngredienteById(id)
                if (ingrediente != null) {
                    _nomeIngrediente.value = ingrediente.ingredient_name
                } else {
                    _nomeIngrediente.value = "Ingrediente com ID $id não encontrado"
                }
            } catch (e: Exception) {
                _nomeIngrediente.value = "Erro: ${e.message}"
            }
        }
    }

    // Função que busca ingredientes por nome e atualiza a lista
    fun searchIngredientes(query: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchIngredientes(query)
                if (results.isNotEmpty()) {
                    val ingredientNames = results.map { it.ingredient_name }.filterNotNull().joinToString(", ")
                    _nomeIngrediente.value = "Resultados: $ingredientNames"
                } else {
                    _nomeIngrediente.value = "Nenhum ingrediente encontrado para '$query'"
                }
            } catch (e: Exception) {
                _nomeIngrediente.value = "Erro na busca: ${e.message}"
            }
        }
    }
}