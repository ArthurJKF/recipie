package com.example.recipie.data.local.dao

import androidx.room.*
import com.example.recipie.data.local.entity.RecipeIngredientEntity
import com.example.recipie.data.model.DetailIngredient

@Dao
interface RecipeIngredientDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(ingredients: List<RecipeIngredientEntity>)

    @Query("SELECT COUNT(*) FROM recipe_ingredients WHERE ingredient_id = :id")
    suspend fun usageCount(id: Long): Int

    @Query("DELETE FROM recipe_ingredients WHERE recipe_id = :id")
    suspend fun deleteByRecipeId(id: Long)

    @Query("""SELECT ri.ingredient_id AS id, COALESCE(i.ingredient_name, 'Ingrediente') AS name,
        ri.quantity, ri.unit, ri.preparation_note AS note,
        EXISTS(SELECT 1 FROM geladeira g WHERE g.food_code = ri.ingredient_id
            AND g.quantity_g > 0 AND (g.expiration_date IS NULL OR g.expiration_date >= :today)) AS available
        FROM recipe_ingredients ri LEFT JOIN ingredientes i ON i.ingredient_id = ri.ingredient_id
        WHERE ri.recipe_id = :id AND i.is_visible = 1 ORDER BY i.ingredient_name""")
    suspend fun details(id: Long, today: String): List<DetailIngredient>
}
