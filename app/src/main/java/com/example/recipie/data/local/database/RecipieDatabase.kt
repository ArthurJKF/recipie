package com.example.recipie.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

import com.example.recipie.data.local.dao.RecipeDao
import com.example.recipie.data.local.dao.IngredienteDao
import com.example.recipie.data.local.dao.RecipeIngredientDao

import com.example.recipie.data.local.entity.RecipeEntity
import com.example.recipie.data.local.entity.IngredienteEntity
import com.example.recipie.data.local.entity.RecipeIngredientEntity

@Database(
    entities = [
        RecipeEntity::class,
        IngredienteEntity::class,
        RecipeIngredientEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class RecipieDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun ingredienteDao(): IngredienteDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
}
