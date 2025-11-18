package com.example.myapplication.todo.ui.item

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MyApplication
import com.example.myapplication.core.Result
import com.example.myapplication.core.TAG
import com.example.myapplication.todo.data.Item
import com.example.myapplication.todo.data.ItemRepository
import kotlinx.coroutines.launch
import java.util.Date

data class ItemUiState(
    val itemId: String? = null,
    val item: Item? = null,
    var submitResult: Result<Item>? = null,
)

class ItemViewModel(private val itemId: String?, private val itemRepository: ItemRepository) : ViewModel() {


    init {
        Log.d(TAG, "init - itemId: $itemId")
    }


    var uiState: ItemUiState by mutableStateOf(
        ItemUiState(item = itemRepository.getItem(itemId)?.copy() ?: Item())
    )
        private set

    fun saveOrUpdateItem(
        name: String,
        description: String,
        noEmployeesStr: String,
        openingDateMillis: String,
        isPublic: Boolean
    ) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateItem - started")
            try {
                uiState = uiState.copy(submitResult = Result.Loading)
                val noEmployees = noEmployeesStr.toIntOrNull() ?: 1
                val openingDate = openingDateMillis.toLongOrNull()?.let { Date(it) } ?: Date()

                val itemToSave = uiState.item?.copy(
                    name = name,
                    description = description,
                    noEmployees = noEmployees,
                    openingDate = openingDate,
                    isPublic = isPublic
                ) ?: return@launch

                val savedItem = if (itemId == null) {
                    itemRepository.save(itemToSave)
                } else {
                    itemRepository.update(itemToSave)
                }

                Log.d(TAG, "saveOrUpdateItem - success")
                uiState = uiState.copy(submitResult = Result.Success(savedItem))
            } catch (e: Exception) {
                Log.d(TAG, "saveOrUpdateItem - error: ${e.message}")
                uiState = uiState.copy(submitResult = Result.Error(e))
            }
        }
    }

    companion object {
        fun Factory(itemId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ItemViewModel(itemId, app.container.itemRepository)
            }
        }
    }
}
