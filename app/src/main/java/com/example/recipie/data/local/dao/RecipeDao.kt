package com.example.recipie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.recipie.data.local.entity.RecipeEntity

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: Long): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE category = :category")
    suspend fun getRecipesByCategory(category: String): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE name LIKE :searchQuery")
    suspend fun searchRecipes(searchQuery: String): List<RecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()
}
