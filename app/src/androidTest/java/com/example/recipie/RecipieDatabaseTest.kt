package com.example.recipie

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.recipie.data.local.database.*
import com.example.recipie.data.model.*
import com.example.recipie.data.repository.RecipieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RecipieDatabaseTest {
    @Test
    fun realAssetMigrationAndUserOperations() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "recipie-test-${UUID.randomUUID()}.db"
        fun open() = Room.databaseBuilder(context, RecipieDatabase::class.java, name)
            .createFromAsset("database/recipie.db")
            .addMigrations(RecipieDatabaseProvider.MIGRATION_1_2, PortugueseCatalogMigration(context), CulinaryTranslationMigration(context)).build()
        var database = open()
        try {
            // Abrir pelo Room verifica automaticamente tabelas, tipos, índices e chaves.
            withContext(Dispatchers.IO) { database.openHelper.writableDatabase }
            var repository = RecipieRepository(database)
            val before = withContext(Dispatchers.IO) {
                database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM recipes").use {
                    it.moveToFirst(); it.getInt(0)
                }
            }
            assertEquals(35516, before)
            repository.addCustomIngredient("Ingrediente exclusivo de teste", 150.0, null)
            val ingredient = repository.searchIngredients("Ingrediente exclusivo de teste").first().single()
            val id = ingredient.ingredient.ingredient_id
            assertTrue(ingredient.personalized)
            assertEquals(150.0, repository.fridge.first().single().entry.quantityG, 0.001)
            val recipeId = repository.addCustomRecipe(RecipeDraft(
                name = "Receita exclusiva de teste", category = "Teste", instructions = "Misture e sirva.",
                ingredients = listOf(RecipeIngredientDraft(id, "Ingrediente exclusivo de teste", "1", "unidade"))))
            repository.toggleFavorite(recipeId)
            assertEquals(recipeId, repository.searchRecipes(RecipeFilters(query = "Receita exclusiva", favoritesOnly = true)).first().single().recipe.id)
            assertTrue(repository.detail(recipeId)!!.ingredients.single().available)
            assertTrue(runCatching { repository.deleteCustomIngredient(id) }.isFailure)
            assertTrue(runCatching { repository.addCustomIngredient("Ingrediente exclusivo de teste", 100.0, null) }.isFailure)
            assertTrue(runCatching { repository.addCustomIngredient("Item inválido de teste", -1.0, null) }.isFailure)
            assertTrue(repository.searchIngredients("Item inválido de teste").first().isEmpty())
            // Confirma persistência após fechar e reabrir o arquivo.
            database.close()
            database = open()
            repository = RecipieRepository(database)
            assertTrue(repository.detail(recipeId)!!.favorite)
            assertEquals(1, repository.fridge.first().size)
            repository.deleteCustomRecipe(recipeId)
            assertNull(repository.detail(recipeId))
            assertTrue(repository.searchRecipes(RecipeFilters(favoritesOnly = true, useFridge = false)).first().isEmpty())
            repository.deleteCustomIngredient(id)
            assertTrue(repository.fridge.first().isEmpty())
            assertTrue(repository.searchIngredients("Ingrediente exclusivo de teste").first().isEmpty())
            assertTrue(runCatching { repository.deleteCustomRecipe(1) }.isFailure)
            assertNotNull(repository.detail(1))
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
