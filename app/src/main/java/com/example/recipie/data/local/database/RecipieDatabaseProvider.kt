package com.example.recipie.data.local.database

import android.content.Context
import androidx.room.Room

object RecipieDatabaseProvider {

    @Volatile
    private var INSTANCE: RecipieDatabase? = null

    fun getDatabase(context: Context): RecipieDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                RecipieDatabase::class.java,
                "recipie.db"
            )
                .createFromAsset("database/recipie.db")
                .build()
                .also { INSTANCE = it }
        }
    }
}
