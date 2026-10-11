package com.example.recipie.data.local.entity

data class GeladeiraItemInfo(
    val id: Long,
    val foodCode: String,
    val descricao: String,
    val classe: String,
    val quantityG: Double,
    val expirationDate: String?,
    val addedAt: Long,
    val energiaKcal: Double?,
    val proteinaG: Double?
)

