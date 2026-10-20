package com.example.recipie.data.local.database

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.recipie.data.model.PortugueseText
import org.json.JSONObject

class PortugueseCatalogMigration(private val context: Context) : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ingredientes ADD COLUMN original_name TEXT")
        db.execSQL("ALTER TABLE ingredientes ADD COLUMN name_search TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE ingredientes ADD COLUMN is_visible INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE ingredientes ADD COLUMN canonical_id INTEGER")
        db.execSQL("ALTER TABLE recipes ADD COLUMN original_name TEXT")
        db.execSQL("ALTER TABLE recipes ADD COLUMN name_search TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE recipes ADD COLUMN summary_pt TEXT")
        db.execSQL("ALTER TABLE recipes ADD COLUMN ingredients_pt TEXT")
        db.execSQL("ALTER TABLE recipes ADD COLUMN instructions_pt TEXT")
        val catalog = context.assets.open("database/catalogo_pt.json").bufferedReader().use { JSONObject(it.readText()) }
        val ingredients = catalog.getJSONArray("ingredients")
        db.compileStatement("""UPDATE ingredientes SET original_name = ingredient_name,
            ingredient_name = ?, name_search = ?, is_visible = ?, canonical_id = ? WHERE ingredient_id = ?""").use { statement ->
            for (index in 0 until ingredients.length()) {
                val row = ingredients.getJSONArray(index)
                val id = row.getLong(0)
                val name = if (row.isNull(1)) null else row.getString(1)
                val canonical = if (row.isNull(2)) null else row.getLong(2)
                statement.clearBindings()
                if (name == null) statement.bindNull(1) else statement.bindString(1, name)
                statement.bindString(2, PortugueseText.key(name.orEmpty()))
                statement.bindLong(3, if (name != null && canonical == id) 1 else 0)
                if (canonical == null) statement.bindNull(4) else statement.bindLong(4, canonical)
                statement.bindLong(5, id)
                statement.executeUpdateDelete()
            }
        }
        // IDs duplicados passam a apontar para o mesmo alimento. Todas as linhas de preparo são preservadas.
        db.execSQL("""UPDATE recipe_ingredients SET ingredient_id = COALESCE(
            (SELECT canonical_id FROM ingredientes WHERE ingredient_id = recipe_ingredients.ingredient_id), ingredient_id)""")
        db.execSQL("CREATE TABLE geladeira_pt_backup AS SELECT * FROM geladeira")
        db.execSQL("DELETE FROM geladeira")
        db.execSQL("""INSERT INTO geladeira(id, food_code, quantity_g, expiration_date, added_at)
            SELECT MIN(g.id), COALESCE(i.canonical_id, g.food_code), SUM(g.quantity_g),
                MIN(g.expiration_date), MIN(g.added_at)
            FROM geladeira_pt_backup g JOIN ingredientes i ON i.ingredient_id = g.food_code
            GROUP BY COALESCE(i.canonical_id, g.food_code)""")
        db.execSQL("DROP TABLE geladeira_pt_backup")
        val recipes = catalog.getJSONArray("recipes")
        db.compileStatement("UPDATE recipes SET original_name = name, name = ?, name_search = ? WHERE id = ?").use { statement ->
            for (index in 0 until recipes.length()) {
                val row = recipes.getJSONArray(index)
                val name = row.getString(1)
                statement.clearBindings()
                statement.bindString(1, name)
                statement.bindString(2, PortugueseText.key(name))
                statement.bindLong(3, row.getLong(0))
                statement.executeUpdateDelete()
            }
        }
        val categories = mapOf(
            "appetizers-and-snacks" to "Entradas e petiscos", "bbq-grilling" to "Churrasco",
            "bread" to "Pães", "breakfast-and-brunch" to "Café da manhã", "desserts" to "Sobremesas",
            "drinks" to "Bebidas", "everyday-cooking" to "Cozinha do dia a dia",
            "fruits-and-vegetables" to "Frutas e legumes", "holidays-and-events" to "Festas e eventos",
            "ingredients" to "Preparos básicos", "main-dish" to "Pratos principais",
            "meat-and-poultry" to "Carnes e aves", "pasta-and-noodles" to "Massas",
            "salad" to "Saladas", "seafood" to "Peixes e frutos do mar", "side-dish" to "Acompanhamentos",
            "soups-stews-and-chili" to "Sopas e ensopados", "trusted-brands-recipes-and-tips" to "Marcas e dicas",
            "uncategorized" to "Outras receitas", "world-cuisine" to "Cozinha internacional",
            "251" to "Outras receitas", "515" to "Outras receitas"
        )
        db.compileStatement("UPDATE recipes SET category = ? WHERE category = ? AND id > 0").use { statement ->
            categories.forEach { (source, target) ->
                statement.bindString(1, target); statement.bindString(2, source); statement.executeUpdateDelete()
            }
        }
        // Itens personalizados já existentes ficam intactos e também ganham busca sem acentos.
        for ((table, idColumn, nameColumn) in listOf(Triple("ingredientes", "ingredient_id", "ingredient_name"), Triple("recipes", "id", "name"))) {
            db.query("SELECT $idColumn, $nameColumn FROM $table WHERE name_search = ''").use { cursor ->
                db.compileStatement("UPDATE $table SET name_search = ? WHERE $idColumn = ?").use { statement ->
                    while (cursor.moveToNext()) {
                        statement.bindString(1, PortugueseText.key(cursor.getString(1).orEmpty()))
                        statement.bindLong(2, cursor.getLong(0)); statement.executeUpdateDelete()
                    }
                }
            }
        }
    }
}
