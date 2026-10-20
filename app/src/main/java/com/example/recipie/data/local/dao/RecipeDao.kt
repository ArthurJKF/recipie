package com.example.recipie.data.local.dao

import androidx.room.*
import com.example.recipie.data.local.entity.RecipeEntity
import com.example.recipie.data.model.RecipeMatch
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Query("SELECT MIN(id) FROM recipes")
    suspend fun minId(): Long?

    @Query("SELECT DISTINCT category FROM recipes WHERE category IS NOT NULL AND TRIM(category) != '' ORDER BY category")
    fun categories(): Flow<List<String>>

    @Query("""SELECT r.*,
        EXISTS(SELECT 1 FROM favorites f WHERE f.recipe_id = r.id) AS favorite,
        EXISTS(SELECT 1 FROM fogao p WHERE p.id = r.id) AS personalized,
        (SELECT COUNT(DISTINCT ri.ingredient_id) FROM recipe_ingredients ri
            JOIN ingredientes i ON i.ingredient_id = ri.ingredient_id AND i.is_visible = 1
            WHERE ri.recipe_id = r.id) AS requiredCount,
        (SELECT COUNT(DISTINCT ri.ingredient_id) FROM recipe_ingredients ri
            JOIN ingredientes i ON i.ingredient_id = ri.ingredient_id AND i.is_visible = 1
            JOIN geladeira g ON g.food_code = ri.ingredient_id AND g.quantity_g > 0
            AND (g.expiration_date IS NULL OR g.expiration_date >= :today)
            WHERE ri.recipe_id = r.id) AS availableCount
        FROM recipes r
        WHERE (r.name_search LIKE :query ESCAPE '\'
            OR EXISTS(SELECT 1 FROM recipe_ingredients ri JOIN ingredientes i
                ON i.ingredient_id = ri.ingredient_id WHERE ri.recipe_id = r.id
                AND i.is_visible = 1 AND i.name_search LIKE :query ESCAPE '\'))
        AND (:category = '' OR r.category = :category)
        AND (:favoritesOnly = 0 OR EXISTS(SELECT 1 FROM favorites f WHERE f.recipe_id = r.id))
        AND (:customOnly = 0 OR EXISTS(SELECT 1 FROM fogao p WHERE p.id = r.id))
        AND (:useFridge = 0 OR EXISTS(SELECT 1 FROM recipe_ingredients ri
            JOIN ingredientes i ON i.ingredient_id = ri.ingredient_id AND i.is_visible = 1
            JOIN geladeira g ON g.food_code = ri.ingredient_id AND g.quantity_g > 0
            AND (g.expiration_date IS NULL OR g.expiration_date >= :today)
            WHERE ri.recipe_id = r.id))
        AND (:completeOnly = 0 OR (
            EXISTS(SELECT 1 FROM recipe_ingredients ri JOIN ingredientes i ON i.ingredient_id = ri.ingredient_id
                WHERE ri.recipe_id = r.id AND i.is_visible = 1)
            AND NOT EXISTS(SELECT 1 FROM recipe_ingredients ri JOIN ingredientes i ON i.ingredient_id = ri.ingredient_id
                WHERE ri.recipe_id = r.id AND i.is_visible = 1
                AND NOT EXISTS(SELECT 1 FROM geladeira g WHERE g.food_code = ri.ingredient_id
                    AND g.quantity_g > 0 AND (g.expiration_date IS NULL OR g.expiration_date >= :today)))))
        ORDER BY availableCount DESC, r.name COLLATE NOCASE LIMIT 100""")
    fun searchRecipes(query: String, category: String, favoritesOnly: Boolean,
        customOnly: Boolean, useFridge: Boolean, completeOnly: Boolean,
        today: String): Flow<List<RecipeMatch>>

    @Query("UPDATE recipes SET summary_pt = :summary, ingredients_pt = :ingredients, instructions_pt = :instructions WHERE id = :id")
    suspend fun cachePortuguese(id: Long, summary: String?, ingredients: String?, instructions: String?)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
