package com.example.recipie.data.local.entity

import androidx.room.*

// Marca quais receitas pertencem ao usuário; o catálogo original não é apagado.
@Entity(tableName = "fogao")
data class FogaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val ingredients: String,
    val instructions: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
