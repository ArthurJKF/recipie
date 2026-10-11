package com.example.recipie.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.recipie.data.local.entity.GeladeiraEntity
import com.example.recipie.data.local.entity.GeladeiraItemInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface GeladeiraDao {

    @Query("""
        SELECT
            g.id,
            g.food_code AS foodCode,
            a.descricao,
            a.classe,
            g.quantity_g AS quantityG,
            g.expiration_date AS expirationDate,
            g.added_at AS addedAt,
            a.energia_kcal AS energiaKcal,
            a.proteina_g AS proteinaG
        FROM geladeira AS g
        INNER JOIN ingredientes AS a
            ON g.food_code = a.codigo
        ORDER BY a.descricao ASC
    """)
    fun getAllItems(): Flow<List<GeladeiraItemInfo>>

    // Find one fridge item
    @Query("SELECT * FROM geladeira WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): GeladeiraEntity?

    // Add a food to the fridge
    @Insert
    suspend fun insertItem(item: GeladeiraEntity): Long

    // Update its quantity or expiration date
    @Update
    suspend fun updateItem(item: GeladeiraEntity)

    // Remove a food from the fridge
    @Delete
    suspend fun deleteItem(item: GeladeiraEntity)
}
