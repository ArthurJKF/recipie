package com.example.recipie.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

import com.example.recipie.data.local.dao.FavoriteDao
import com.example.recipie.data.local.dao.FogaoDao
import com.example.recipie.data.local.dao.GeladeiraDao
import com.example.recipie.data.local.dao.RecipeDao
import com.example.recipie.data.local.dao.IngredientesDao

import com.example.recipie.data.local.entity.FavoriteEntity
import com.example.recipie.data.local.entity.FogaoEntity
import com.example.recipie.data.local.entity.GeladeiraEntity
import com.example.recipie.data.local.entity.RecipeEntity
import com.example.recipie.data.local.entity.IngredientesEntity

@Database(
    entities = [
        RecipeEntity::class,
        FavoriteEntity::class,
        FogaoEntity::class,
        IngredientesEntity::class,
        GeladeiraEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class RecipieDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun fogaoDao(): FogaoDao

    abstract fun tabelaAlimentosDao(): IngredientesDao

    abstract fun geladeiraDao(): GeladeiraDao
}
