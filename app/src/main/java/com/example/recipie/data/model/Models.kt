package com.example.recipie.data.model

import androidx.room.Embedded
import com.example.recipie.data.local.entity.*

data class CatalogIngredient(@Embedded val ingredient: IngredienteEntity, val personalized: Boolean)
data class FridgeItem(@Embedded val entry: GeladeiraEntity, val name: String, val personalized: Boolean)
data class RecipeMatch(@Embedded val recipe: RecipeEntity, val favorite: Boolean,
    val personalized: Boolean, val requiredCount: Int, val availableCount: Int)
data class DetailIngredient(val id: Long, val name: String, val quantity: String?,
    val unit: String?, val note: String?, val available: Boolean)
data class RecipeDetail(val recipe: RecipeEntity, val ingredients: List<DetailIngredient>,
    val favorite: Boolean, val personalized: Boolean)
data class RecipeFilters(val query: String = "", val category: String = "",
    val favoritesOnly: Boolean = false, val customOnly: Boolean = false,
    val useFridge: Boolean = true, val completeOnly: Boolean = false)
data class RecipeIngredientDraft(val ingredientId: Long, val name: String,
    val quantity: String = "", val unit: String = "", val note: String = "")
data class RecipeDraft(val name: String = "", val category: String = "Personalizada",
    val summary: String = "", val instructions: String = "", val prepMinutes: String = "",
    val cookMinutes: String = "", val servings: String = "",
    val ingredients: List<RecipeIngredientDraft> = emptyList())
