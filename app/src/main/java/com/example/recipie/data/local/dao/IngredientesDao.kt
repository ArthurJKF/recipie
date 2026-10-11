package com.example.recipie.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.recipie.data.local.entity.IngredientesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientesDao {

    @Query("""
        SELECT * FROM ingredientes
        ORDER BY descricao ASC
    """)
    fun getAllFoods(): Flow<List<IngredientesEntity>>

    @Query("""
        SELECT * FROM ingredientes
        WHERE descricao LIKE '%' || :search || '%'
        ORDER BY descricao ASC
    """)
    fun searchFoods(search: String): Flow<List<IngredientesEntity>>

    @Query("""
        SELECT * FROM ingredientes
        WHERE codigo = :codigo
        LIMIT 1
    """)
    suspend fun getFoodByCode(codigo: String): IngredientesEntity?

    @Query("""
        SELECT * FROM ingredientes
        WHERE classe = :classe
        ORDER BY descricao ASC
    """)
    fun getFoodsByCategory(classe: String): Flow<List<IngredientesEntity>>

    @Query("""
        SELECT DISTINCT classe FROM ingredientes
        ORDER BY classe ASC
    """)
    fun getCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM ingredientes")
    suspend fun getFoodCount(): Int
}

