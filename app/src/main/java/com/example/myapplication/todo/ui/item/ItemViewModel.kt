package com.example.myapp.todo.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.ItemRepository
import java.util.Date

data class ItemUiState(val item: Item? = null)

class ItemViewModel(private val itemId: String) : ViewModel() {
    var uiState by mutableStateOf(ItemUiState())
        private set

    init {
        loadItem()
    }

    private fun loadItem() {
        uiState = ItemUiState(item = ItemRepository.items.find { it.id == itemId })
    }

    fun updateItem(
        name: String,
        description: String,
        noEmployeesStr: String,
        openingDateStr: String,
        isPublic: Boolean
    ) {
        val noEmployees = noEmployeesStr.toIntOrNull() ?: 1
        val openingDate = openingDateStr.toLongOrNull()?.let { Date(it) } ?: Date()
        val updated = ItemRepository.update(
            id = itemId,
            name = name,
            description = description,
            noEmployees = noEmployees,
            openingDate = openingDate,
            isPublic = isPublic
        )
        if (updated != null) {
            uiState = ItemUiState(item = updated)
        }
    }
}

class ItemViewModelFactory(private val itemId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ItemViewModel(itemId) as T
    }
}
