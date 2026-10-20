package com.example.recipie.data.local.entity

import androidx.room.*

@Entity(tableName = "ingredientes")
data class IngredienteEntity(
    @PrimaryKey val ingredient_id: Long,
    val ingredient_name: String?,
    @ColumnInfo(name = "is_custom", defaultValue = "0") val isCustom: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long? = null,
    val note: String? = null,
    @ColumnInfo(name = "original_name") val originalName: String? = null,
    @ColumnInfo(name = "name_search", defaultValue = "''") val nameSearch: String = "",
    @ColumnInfo(name = "is_visible", defaultValue = "1") val isVisible: Boolean = true,
    @ColumnInfo(name = "canonical_id") val canonicalId: Long? = null
)
