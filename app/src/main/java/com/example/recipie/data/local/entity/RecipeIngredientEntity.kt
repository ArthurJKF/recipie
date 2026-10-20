package com.example.recipie.data.local.entity

import androidx.room.*

// Uma receita pode usar o mesmo ingrediente em etapas diferentes.
@Entity(tableName = "recipe_ingredients", indices = [Index("recipe_id"), Index("ingredient_id")])
data class RecipeIngredientEntity(
    val recipe_id: Long,
    val ingredient_id: Long,
    val quantity: String?,
    val unit: String?,
    val preparation_note: String?,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
