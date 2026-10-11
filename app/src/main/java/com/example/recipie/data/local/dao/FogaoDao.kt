
package com.example.recipie.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.recipie.data.local.entity.FogaoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FogaoDao {

    @Query("SELECT * FROM fogao ORDER BY created_at DESC")
    fun getAllRecipes(): Flow<List<FogaoEntity>>

    @Query("SELECT * FROM fogao WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: Long): FogaoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: FogaoEntity): Long

    @Update
    suspend fun updateRecipe(recipe: FogaoEntity)

    @Delete
    suspend fun deleteRecipe(recipe: FogaoEntity)
}

