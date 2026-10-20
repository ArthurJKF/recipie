package com.example.recipie.data.local.database

import androidx.room.*
import com.example.recipie.data.local.dao.*
import com.example.recipie.data.local.entity.*

@Database(entities = [RecipeEntity::class, IngredienteEntity::class,
    RecipeIngredientEntity::class, GeladeiraEntity::class, FavoriteEntity::class,
    FogaoEntity::class], version = 4, exportSchema = true)
abstract class RecipieDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredienteDao(): IngredienteDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun userDataDao(): UserDataDao
}
