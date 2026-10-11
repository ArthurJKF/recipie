package com.example.recipie.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.recipie.data.local.database.RecipieDatabaseProvider
import com.example.recipie.data.local.entity.RecipeEntity
import com.example.recipie.data.repository.RecipieRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipieViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipieRepository
    private val _searchResults = MutableLiveData<List<RecipeEntity>>()
    val searchResults: LiveData<List<RecipeEntity>> = _searchResults
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        val database = RecipieDatabaseProvider.getDatabase(getApplication())
        repository = RecipieRepository(
            database.recipeDao(),
            database.ingredienteDao(),
            database.recipeIngredientDao()
        )
    }

    fun searchRecipesByIngredient(ingredientName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (ingredientName.isNotEmpty()) {
                    // First find ingredients matching the search term
                    val matchingIngredients = repository.searchIngredientes(ingredientName)

                    // If we found matching ingredients, get recipes that use them
                    if (matchingIngredients.isNotEmpty()) {
                        val ingredientIds = matchingIngredients.map { it.ingredient_id }
                        // For simplicity, we'll get recipes for the first matching ingredient
                        // In a more advanced implementation, we'd combine results from all matching ingredients
                        val recipeIngredients = repository.getRecipesByIngredientId(ingredientIds.firstOrNull() ?: -1L)

                        // Get unique recipe IDs from the recipe-ingredient relationships
                        val recipeIds = recipeIngredients.map { it.recipe_id }.distinct()

                        // Fetch full recipe details for each ID
                        val recipes = mutableListOf<RecipeEntity>()
                        for (recipeId in recipeIds) {
                            val recipe = repository.getRecipeById(recipeId)
                            if (recipe != null) {
                                recipes.add(recipe)
                            }
                        }

                        _searchResults.value = recipes
                    } else {
                        // No matching ingredients found
                        _searchResults.value = emptyList()
                    }
                } else {
                    // If search is empty, show all recipes
                    _searchResults.value = repository.getAllRecipes()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAllRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val recipes = repository.getAllRecipes()
                _searchResults.value = recipes
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}