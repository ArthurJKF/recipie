package com.example.recipie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_ingredients", primaryKeys = ["recipe_id", "ingredient_id"])
data class RecipeIngredientEntity(
    val recipe_id: Long,
    val ingredient_id: Long,
    val quantity: String?,
    val unit: String?,
    val preparation_note: String?
)