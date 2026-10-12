package com.example.recipie.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "geladeira",
    foreignKeys = [
        ForeignKey(
            entity = IngredienteEntity::class,
            parentColumns = ["ingredient_id"],
            childColumns = ["food_code"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["food_code"])
    ]
)
data class GeladeiraEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "food_code")
    val foodCode: String,

    @ColumnInfo(name = "quantity_g")
    val quantityG: Double,

    @ColumnInfo(name = "expiration_date")
    val expirationDate: String? = null,

    @ColumnInfo(name = "added_at")
    val addedAt: Long
)