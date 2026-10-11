package com.example.recipie.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.recipie.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("""
        SELECT * FROM recipes
        ORDER BY name ASC, id ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getRecipes(
        limit: Int = 30,
        offset: Int = 0
    ): List<RecipeEntity>

    @Query("""
        SELECT * FROM recipes
        WHERE name LIKE '%' || :search || '%'
        ORDER BY name ASC, id ASC
        LIMIT 50
    """)
    fun searchRecipes(search: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes LIMIT 20")
    suspend fun getFirst20Recipes(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Query("""
        SELECT * FROM recipes
        WHERE category = :category
        ORDER BY name ASC, id ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getRecipesByCategory(
        category: String,
        limit: Int = 30,
        offset: Int = 0
    ): List<RecipeEntity>

    @Query("""
        SELECT DISTINCT category FROM recipes
        WHERE category IS NOT NULL
        ORDER BY category ASC
    """)
    fun getCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int
}
