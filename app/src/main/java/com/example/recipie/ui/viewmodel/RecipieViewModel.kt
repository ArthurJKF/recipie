package com.example.recipie.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipie.data.local.database.RecipieDatabaseProvider
import com.example.recipie.data.model.*
import com.example.recipie.data.repository.RecipieRepository
import com.example.recipie.data.translation.PortugueseTranslator
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class RecipieViewModel(application: Application) : AndroidViewModel(application) {
    private val translator = PortugueseTranslator(application)
    private val _translating = MutableStateFlow(false)
    val translating = _translating.asStateFlow()
    private val _translationError = MutableStateFlow<String?>(null)
    val translationError = _translationError.asStateFlow()
    private val repository = RecipieRepository(RecipieDatabaseProvider.getDatabase(application))
    private val notifications = Channel<String>(Channel.BUFFERED)
    val messages = notifications.receiveAsFlow()
    private val _databaseError = MutableStateFlow<String?>(null)
    val databaseError = _databaseError.asStateFlow()
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError = _actionError.asStateFlow()
    fun clearActionError() { _actionError.value = null }
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _loadingRecipes = MutableStateFlow(true)
    val loadingRecipes = _loadingRecipes.asStateFlow()
    private val _filters = MutableStateFlow(RecipeFilters())
    val filters = _filters.asStateFlow()
    private val _ingredientQuery = MutableStateFlow("")
    val ingredientQuery = _ingredientQuery.asStateFlow()
    private val _detail = MutableStateFlow<RecipeDetail?>(null)
    val detail = _detail.asStateFlow()
    private val _loadingDetail = MutableStateFlow(false)
    val loadingDetail = _loadingDetail.asStateFlow()
    private var detailJob: Job? = null
    private val _draft = MutableStateFlow(RecipeDraft())
    val draft = _draft.asStateFlow()

    private fun <T> Flow<T>.withDatabaseErrors(): Flow<T> = catch { error ->
        if (error is CancellationException) throw error
        _loadingRecipes.value = false
        _databaseError.value = "Não foi possível abrir o banco de dados: ${error.message}"
    }

    val fridge = repository.fridge.withDatabaseErrors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories.withDatabaseErrors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val catalog = _ingredientQuery.debounce(250).flatMapLatest(repository::searchIngredients)
        .withDatabaseErrors().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recipes = _filters.debounce(250).flatMapLatest { filters ->
        repository.searchRecipes(filters).onStart { _loadingRecipes.value = true }
            .onEach { _loadingRecipes.value = false }
    }.withDatabaseErrors().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilters(filters: RecipeFilters) { _filters.value = filters }
    fun searchIngredients(query: String) { _ingredientQuery.value = query }
    fun updateDraft(draft: RecipeDraft) { _draft.value = draft }
    fun resetDraft() { _draft.value = RecipeDraft() }

    private fun action(message: String, onSuccess: () -> Unit = {}, block: suspend () -> Unit) {
        if (_busy.value) return
        _busy.value = true
        _actionError.value = null
        viewModelScope.launch {
            try {
                block()
                onSuccess()
                notifications.send(message)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _actionError.value = error.message ?: "Não foi possível concluir a operação."
                notifications.send(_actionError.value!!)
            } finally { _busy.value = false }
        }
    }

    fun addToFridge(id: Long, quantity: Double, expiration: String?, onSuccess: () -> Unit) =
        action("Ingrediente salvo na geladeira.", onSuccess) { repository.addToFridge(id, quantity, expiration) }

    fun addCustomIngredient(name: String, quantity: Double, expiration: String?, note: String, onSuccess: () -> Unit) =
        action("Ingrediente personalizado criado.", onSuccess) { repository.addCustomIngredient(name, quantity, expiration, note) }

    fun removeFromFridge(id: Long) = action("Ingrediente retirado da geladeira.") { repository.removeFromFridge(id) }
    fun deleteCustomIngredient(id: Long, onSuccess: () -> Unit) =
        action("Ingrediente personalizado excluído.", onSuccess) { repository.deleteCustomIngredient(id) }
    fun saveRecipe(onSuccess: () -> Unit) = action("Receita personalizada salva.", onSuccess) {
        repository.addCustomRecipe(_draft.value)
        _draft.value = RecipeDraft()
    }
    fun deleteRecipe(id: Long, onSuccess: () -> Unit) = action("Receita personalizada excluída.", onSuccess) {
        repository.deleteCustomRecipe(id)
        _detail.value = null
    }
    fun toggleFavorite(id: Long) = action("Favoritos atualizados.") {
        repository.toggleFavorite(id)
        if (_detail.value?.recipe?.id == id) _detail.value = repository.detail(id)
    }
    fun openDetail(id: Long) {
        detailJob?.cancel()
        _detail.value = null
        _loadingDetail.value = true
        detailJob = viewModelScope.launch {
            try {
                _translationError.value = null
                _detail.value = repository.detail(id)
                _loadingDetail.value = false
                val original = repository.originalRecipe(id)
                if (original != null && original.originalName != null && original.instructionsPt == null) {
                    _translating.value = true
                    try {
                        translator.prepare()
                        val summary = translator.translate(original.summary)
                        val ingredients = translator.translate(original.ingredients)
                        val instructions = translator.translate(original.instructions).orEmpty()
                        repository.cachePortuguese(id, summary, ingredients, instructions)
                        _detail.value = repository.detail(id)
                    } catch (error: TimeoutCancellationException) {
                        _translationError.value = "A tradução demorou mais que o esperado. Confira o Wi-Fi e tente novamente."
                    } catch (error: CancellationException) { throw error }
                    catch (error: Exception) {
                        _translationError.value = "Conecte o celular ao Wi-Fi para preparar a tradução em português. Depois, ela funciona sem internet."
                    } finally { _translating.value = false }
                }
            }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) { notifications.send(error.message ?: "Erro ao abrir a receita.") }
            finally { _loadingDetail.value = false }
        }
    }
    fun closeDetail() {
        detailJob?.cancel()
        _detail.value = null
        _loadingDetail.value = false
        _translating.value = false
    }

    override fun onCleared() { translator.close(); super.onCleared() }
}
