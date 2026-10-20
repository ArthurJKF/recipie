package com.example.recipie.data.local.dao

import androidx.room.*
import com.example.recipie.data.local.entity.*
import com.example.recipie.data.model.FridgeItem
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {
    @Query("""SELECT g.*, COALESCE(i.ingredient_name, 'Ingrediente') AS name,
        i.is_custom AS personalized
        FROM geladeira g JOIN ingredientes i ON i.ingredient_id = g.food_code
        ORDER BY i.ingredient_name COLLATE NOCASE""")
    fun fridge(): Flow<List<FridgeItem>>

    @Query("SELECT * FROM geladeira WHERE food_code = :id LIMIT 1")
    suspend fun fridgeEntry(id: Long): GeladeiraEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFridge(item: GeladeiraEntity)

    @Update suspend fun updateFridge(item: GeladeiraEntity)

    @Query("DELETE FROM geladeira WHERE food_code = :id")
    suspend fun removeFromFridge(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE recipe_id = :id")
    suspend fun removeFavorite(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE recipe_id = :id)")
    suspend fun isFavorite(id: Long): Boolean

    @Query("DELETE FROM fogao WHERE id = :id")
    suspend fun removeCustomRecipe(id: Long)

    @Insert suspend fun insertCustomRecipe(recipe: FogaoEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM ingredientes WHERE ingredient_id = :id AND is_custom = 1)")
    suspend fun isCustomIngredient(id: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM fogao WHERE id = :id)")
    suspend fun isCustomRecipe(id: Long): Boolean
}
