package com.example.recipie.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RecipieDatabaseProvider {
    @Volatile private var instance: RecipieDatabase? = null

    // Mantém as três tabelas originais e acrescenta somente os dados do usuário.
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // O arquivo enviado tem food_code TEXT apontando para codigo (inexistente).
            // Guardamos os dados antes de reconstruir apenas o esquema incompatível.
            db.execSQL("CREATE TABLE geladeira_backup AS SELECT * FROM geladeira")
            db.execSQL("DROP TABLE geladeira")
            db.execSQL("""CREATE TABLE ingredientes_new (ingredient_id INTEGER NOT NULL PRIMARY KEY,
                ingredient_name TEXT, is_custom INTEGER NOT NULL DEFAULT 0, created_at INTEGER, note TEXT)""")
            db.execSQL("INSERT INTO ingredientes_new (ingredient_id, ingredient_name) SELECT ingredient_id, ingredient_name FROM ingredientes")
            db.execSQL("DROP TABLE ingredientes")
            db.execSQL("ALTER TABLE ingredientes_new RENAME TO ingredientes")
            db.execSQL("""CREATE TABLE recipe_ingredients_new (
                recipe_id INTEGER NOT NULL, ingredient_id INTEGER NOT NULL,
                quantity TEXT, unit TEXT, preparation_note TEXT,
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT)""")
            db.execSQL("""INSERT INTO recipe_ingredients_new (recipe_id, ingredient_id, quantity, unit, preparation_note)
                SELECT recipe_id, ingredient_id, quantity, unit, preparation_note FROM recipe_ingredients""")
            db.execSQL("DROP TABLE recipe_ingredients")
            db.execSQL("ALTER TABLE recipe_ingredients_new RENAME TO recipe_ingredients")
            db.execSQL("CREATE INDEX index_recipe_ingredients_recipe_id ON recipe_ingredients(recipe_id)")
            db.execSQL("CREATE INDEX index_recipe_ingredients_ingredient_id ON recipe_ingredients(ingredient_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_recipes_name ON recipes(name)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_recipes_category ON recipes(category)")
            db.execSQL("""CREATE TABLE geladeira (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                food_code INTEGER NOT NULL, quantity_g REAL NOT NULL,
                expiration_date TEXT, added_at INTEGER NOT NULL,
                FOREIGN KEY(food_code) REFERENCES ingredientes(ingredient_id) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("""INSERT INTO geladeira (id, food_code, quantity_g, expiration_date, added_at)
                SELECT MIN(id), CAST(food_code AS INTEGER), SUM(quantity_g), MIN(expiration_date), MIN(added_at)
                FROM geladeira_backup GROUP BY CAST(food_code AS INTEGER)""")
            db.execSQL("DROP TABLE geladeira_backup")
            db.execSQL("CREATE UNIQUE INDEX index_geladeira_food_code ON geladeira(food_code)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS favorites (
                recipe_id INTEGER NOT NULL PRIMARY KEY, added_at INTEGER NOT NULL,
                FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            db.execSQL("""CREATE TABLE IF NOT EXISTS fogao (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,
                ingredients TEXT NOT NULL, instructions TEXT NOT NULL, created_at INTEGER NOT NULL)""")

        }
    }

    fun getDatabase(context: Context): RecipieDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(context.applicationContext,
            RecipieDatabase::class.java, "recipie.db")
            .createFromAsset("database/recipie.db")
            .addMigrations(MIGRATION_1_2, PortugueseCatalogMigration(context.applicationContext), CulinaryTranslationMigration(context.applicationContext))
            .build().also { instance = it }
    }
}
