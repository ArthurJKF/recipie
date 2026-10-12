package com.example.recipie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.recipie.data.local.entity.IngredienteEntity

@Dao
interface IngredienteDao {

    @Query("SELECT * FROM ingredientes")
    suspend fun getAllIngredientes(): List<IngredienteEntity>

    @Query("SELECT * FROM ingredientes WHERE ingredient_id = :ingredientId")
    suspend fun getIngredienteById(ingredientId: Long): IngredienteEntity?

    @Query("SELECT * FROM ingredientes WHERE ingredient_name LIKE :searchQuery")
    suspend fun searchIngredientes(searchQuery: String): List<IngredienteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredientes: List<IngredienteEntity>)

    @Update
    suspend fun update(ingrediente: IngredienteEntity)

    @Query("DELETE FROM ingredientes")
    suspend fun deleteAll()
}
