package com.example.recipie.data.local.database

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.recipie.data.model.PortugueseText
import org.json.JSONObject

class CulinaryTranslationMigration(private val context: Context) : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // O texto antigo fica no original; somente o cache de traduções automáticas é renovado.
        db.execSQL("UPDATE recipes SET summary_pt = NULL, ingredients_pt = NULL, instructions_pt = NULL WHERE original_name IS NOT NULL")
        val catalog = context.assets.open("database/catalogo_revisado.json").bufferedReader().use { JSONObject(it.readText()) }
        val recipes = catalog.getJSONArray("recipes")
        db.compileStatement("UPDATE recipes SET name = ?, name_search = ? WHERE id = ? AND original_name IS NOT NULL").use { statement ->
            for (index in 0 until recipes.length()) {
                val row = recipes.getJSONArray(index)
                statement.bindString(1, row.getString(1)); statement.bindString(2, PortugueseText.key(row.getString(1)))
                statement.bindLong(3, row.getLong(0)); statement.executeUpdateDelete()
            }
        }
        val ingredients = catalog.getJSONArray("ingredients")
        db.compileStatement("UPDATE ingredientes SET ingredient_name = ?, name_search = ? WHERE canonical_id = ? AND is_custom = 0").use { statement ->
            for (index in 0 until ingredients.length()) {
                val row = ingredients.getJSONArray(index)
                statement.bindString(1, row.getString(1)); statement.bindString(2, PortugueseText.key(row.getString(1)))
                statement.bindLong(3, row.getLong(0)); statement.executeUpdateDelete()
            }
        }
        val reviewed = context.assets.open("translation/reviewed_recipes.json").bufferedReader().use { JSONObject(it.readText()).getJSONArray("recipes") }
        db.compileStatement("UPDATE recipes SET name = ?, name_search = ?, summary_pt = ?, ingredients_pt = ?, instructions_pt = ? WHERE id = ? AND original_name IS NOT NULL").use { statement ->
            for (index in 0 until reviewed.length()) {
                val row = reviewed.getJSONObject(index)
                statement.bindString(1, row.getString("name")); statement.bindString(2, PortugueseText.key(row.getString("name")))
                statement.bindString(3, row.getString("summary")); statement.bindString(4, row.getString("ingredients"))
                statement.bindString(5, row.getString("instructions")); statement.bindLong(6, row.getLong("id"))
                statement.executeUpdateDelete()
            }
        }
    }
}
