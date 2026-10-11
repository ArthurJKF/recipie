package com.example.recipie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredientes")
data class IngredienteEntity(
    @PrimaryKey
    val ingredient_id: Long,
    val ingredient_name: String?
)