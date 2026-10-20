package com.example.recipie.data.repository

import androidx.room.withTransaction
import com.example.recipie.data.local.database.RecipieDatabase
import com.example.recipie.data.local.entity.*
import com.example.recipie.data.model.*
import java.time.LocalDate

class RecipieRepository(private val database: RecipieDatabase) {
    private val ingredients = database.ingredienteDao()
    private val recipes = database.recipeDao()
    private val relations = database.recipeIngredientDao()
    private val user = database.userDataDao()

    val fridge = user.fridge()
    val categories = recipes.categories()

    private fun like(query: String): String {
        val normalized = PortugueseText.key(query)
        val singular = mapOf("ovos" to "ovo", "cebolas" to "cebola", "tomates" to "tomate",
            "limões" to "limao", "limoes" to "limao", "morangos" to "morango")[normalized] ?: normalized
        return "%" + singular.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"
    }

    fun searchIngredients(query: String) = ingredients.searchIngredientes(like(query))
    fun searchRecipes(filters: RecipeFilters) = recipes.searchRecipes(like(filters.query),
        filters.category, filters.favoritesOnly, filters.customOnly, filters.useFridge,
        filters.completeOnly, LocalDate.now().toString())

    suspend fun addToFridge(id: Long, quantity: Double, expiration: String?) = database.withTransaction {
        require(quantity.isFinite() && quantity > 0) { "Informe uma quantidade maior que zero." }
        require(ingredients.getIngredienteById(id) != null) { "Ingrediente não encontrado." }
        val date = expiration?.trim()?.takeIf { it.isNotEmpty() }
        if (date != null) {
            val parsed = runCatching { LocalDate.parse(date) }.getOrNull()
            require(parsed != null && parsed.toString() == date) { "Use a validade no formato AAAA-MM-DD." }
            require(!parsed.isBefore(LocalDate.now())) { "A validade não pode estar no passado." }
        }
        val current = user.fridgeEntry(id)
        if (current == null) user.insertFridge(GeladeiraEntity(foodCode = id, quantityG = quantity, expirationDate = date))
        else user.updateFridge(current.copy(quantityG = quantity, expirationDate = date))
    }

    suspend fun removeFromFridge(id: Long) = user.removeFromFridge(id)

    suspend fun addCustomIngredient(name: String, quantity: Double, expiration: String?, note: String = "") = database.withTransaction {
        val cleaned = name.trim()
        require(cleaned.length in 2..100) { "O nome deve ter entre 2 e 100 caracteres." }
        require(ingredients.findByName(PortugueseText.key(cleaned)) == null) { "Esse ingrediente já existe. Busque-o no catálogo." }
        val id = minOf(ingredients.minId() ?: 0, 0) - 1
        ingredients.insert(IngredienteEntity(id, cleaned, true, System.currentTimeMillis(), note.trim().ifEmpty { null }, nameSearch = PortugueseText.key(cleaned)))
        addToFridge(id, quantity, expiration)
    }

    suspend fun deleteCustomIngredient(id: Long) = database.withTransaction {
        require(user.isCustomIngredient(id)) { "Somente ingredientes personalizados podem ser excluídos do catálogo." }
        require(relations.usageCount(id) == 0) { "O ingrediente está em uma receita. Remova a receita antes de excluí-lo." }
        user.removeFromFridge(id)
        ingredients.deleteById(id)
    }

    suspend fun addCustomRecipe(draft: RecipeDraft): Long = database.withTransaction {
        require(draft.name.trim().length in 2..120) { "O nome deve ter entre 2 e 120 caracteres." }
        require(draft.instructions.trim().isNotEmpty()) { "Preencha o modo de preparo." }
        require(draft.ingredients.isNotEmpty()) { "Adicione ao menos um ingrediente." }
        require(draft.ingredients.distinctBy { it.ingredientId }.size == draft.ingredients.size) { "Não repita ingredientes." }
        for (item in draft.ingredients) {
            require(ingredients.getIngredienteById(item.ingredientId) != null) { "O ingrediente ${item.name} não existe mais." }
            require(item.quantity.toDoubleOrNull()?.let { it.isFinite() && it > 0 } == true) { "Informe a quantidade de ${item.name}." }
            require(item.unit.isNotBlank()) { "Informe a unidade de ${item.name}." }
        }
        fun minutes(text: String): Int? {
            if (text.isBlank()) return null
            return text.toIntOrNull()?.takeIf { it >= 0 && it <= 10080 }
                ?: throw IllegalArgumentException("Informe um tempo inteiro entre 0 e 10080 minutos.")
        }
        val prep = minutes(draft.prepMinutes)
        val cook = minutes(draft.cookMinutes)
        val servings = if (draft.servings.isBlank()) {
            null
        } else {
            draft.servings.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
                ?: throw IllegalArgumentException("Informe uma quantidade de porções maior que zero.")
        }
        val textIngredients = draft.ingredients.joinToString("\n") { "${it.quantity} ${it.unit} de ${it.name}" }
        val id = minOf(recipes.minId() ?: 0, 0) - 1
        recipes.insert(RecipeEntity(id = id, name = draft.name.trim(), nameSearch = PortugueseText.key(draft.name),
            category = draft.category.trim().ifEmpty { "Personalizada" }, author = "Você",
            summary = draft.summary.trim().ifEmpty { null }, ingredients = textIngredients,
            instructions = draft.instructions.trim(), prep = prep?.let { "$it min" },
            cook = cook?.let { "$it min" }, total = if (prep == null && cook == null) null else "${(prep ?: 0) + (cook ?: 0)} min",
            servings = servings))
        relations.insertAll(draft.ingredients.map { RecipeIngredientEntity(id, it.ingredientId,
            it.quantity, it.unit.trim(), it.note.trim().ifEmpty { null }) })
        user.insertCustomRecipe(FogaoEntity(id, draft.name.trim(), textIngredients, draft.instructions.trim()))
        id
    }

    suspend fun deleteCustomRecipe(id: Long) = database.withTransaction {
        require(user.isCustomRecipe(id)) { "Somente receitas personalizadas podem ser excluídas." }
        relations.deleteByRecipeId(id)
        user.removeCustomRecipe(id)
        recipes.deleteById(id)
    }

    suspend fun toggleFavorite(id: Long) = database.withTransaction {
        require(recipes.getRecipeById(id) != null) { "A receita não existe mais." }
        if (user.isFavorite(id)) user.removeFavorite(id)
        else user.insertFavorite(FavoriteEntity(id, System.currentTimeMillis()))
    }

    suspend fun originalRecipe(id: Long) = recipes.getRecipeById(id)
    suspend fun cachePortuguese(id: Long, summary: String?, ingredients: String?, instructions: String?) =
        recipes.cachePortuguese(id, summary, ingredients, instructions)

    suspend fun detail(id: Long): RecipeDetail? = database.withTransaction {
        val recipe = recipes.getRecipeById(id) ?: return@withTransaction null
        val custom = user.isCustomRecipe(id)
        val displayed = if (custom) recipe else recipe.copy(
            summary = recipe.summaryPt, ingredients = recipe.ingredientsPt, instructions = recipe.instructionsPt,
            prep = PortugueseText.time(recipe.prep), cook = PortugueseText.time(recipe.cook), total = PortugueseText.time(recipe.total))
        RecipeDetail(displayed, relations.details(id, LocalDate.now().toString()),
            user.isFavorite(id), custom)
    }
}
