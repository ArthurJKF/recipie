package com.example.recipie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.recipie.data.local.entity.RecipeIngredientEntity

@Dao
interface RecipeIngredientDao {

    @Query("SELECT * FROM recipe_ingredients")
    suspend fun getAllRecipeIngredients(): List<RecipeIngredientEntity>

    @Query("SELECT * FROM recipe_ingredients WHERE recipe_id = :recipeId")
    suspend fun getIngredientsByRecipeId(recipeId: Long): List<RecipeIngredientEntity>

    @Query("SELECT * FROM recipe_ingredients WHERE ingredient_id = :ingredientId")
    suspend fun getRecipesByIngredientId(ingredientId: Long): List<RecipeIngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipeIngredients: List<RecipeIngredientEntity>)

    @Transaction
    suspend fun insertWithRecipeAndIngrediente(recipeId: Long, ingredienteId: Long, quantity: String?, unit: String?, preparationNote: String?) {
        insertAll(listOf(RecipeIngredientEntity(recipeId, ingredienteId, quantity, unit, preparationNote)))
    }

    @Query("DELETE FROM recipe_ingredients WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)

    @Query("DELETE FROM recipe_ingredients WHERE ingredient_id = :ingredientId")
    suspend fun deleteByIngredientId(ingredientId: Long)

    @Query("DELETE FROM recipe_ingredients")
    suspend fun deleteAll()
}
