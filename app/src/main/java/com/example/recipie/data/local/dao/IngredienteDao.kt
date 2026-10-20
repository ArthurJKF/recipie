package com.example.recipie.data.local.dao

import androidx.room.*
import com.example.recipie.data.local.entity.IngredienteEntity
import com.example.recipie.data.model.CatalogIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredienteDao {
    @Query("SELECT * FROM ingredientes WHERE ingredient_id = :id")
    suspend fun getIngredienteById(id: Long): IngredienteEntity?

    @Query("""SELECT i.*, i.is_custom AS personalized
        FROM ingredientes i WHERE i.is_visible = 1 AND i.name_search LIKE :query ESCAPE '\'
        ORDER BY personalized DESC,
        (SELECT COUNT(*) FROM recipe_ingredients ri WHERE ri.ingredient_id = i.ingredient_id) DESC,
        i.ingredient_name COLLATE NOCASE LIMIT 100""")
    fun searchIngredientes(query: String): Flow<List<CatalogIngredient>>

    @Query("SELECT * FROM ingredientes WHERE name_search = :name AND is_visible = 1 LIMIT 1")
    suspend fun findByName(name: String): IngredienteEntity?

    @Query("SELECT MIN(ingredient_id) FROM ingredientes")
    suspend fun minId(): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ingrediente: IngredienteEntity)

    @Query("DELETE FROM ingredientes WHERE ingredient_id = :id")
    suspend fun deleteById(id: Long)
}
