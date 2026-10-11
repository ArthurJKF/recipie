package com.example.recipie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recipie.data.local.entity.FavoriteEntity
import com.example.recipie.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("""
        SELECT r.* FROM recipes AS r
        INNER JOIN favorites AS f
            ON r.id = f.recipe_id
        ORDER BY f.added_at DESC
    """)
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM favorites
            WHERE recipe_id = :recipeId
        )
    """)
    fun isFavorite(recipeId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE recipe_id = :recipeId")
    suspend fun removeFavorite(recipeId: Long)

    @Query("SELECT COUNT(*) FROM favorites")
    fun getFavoriteCount(): Flow<Int>
}
