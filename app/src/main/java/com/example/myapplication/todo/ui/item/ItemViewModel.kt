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
import com.example.myapplication.todo.data.ItemRepo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Locale
import java.util.Date

private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}


data class ItemUiState(
    val itemId: String? = null,
    val item: Item = Item(),
    var submitResult: Result<Item>? = null,
    var loadResult: Result<Item>? = null
    )

class ItemViewModel(private val itemId: String?, private val itemRepository: ItemRepo) : ViewModel() {

    var uiState: ItemUiState by mutableStateOf(ItemUiState(loadResult = Result.Loading))
        private set


    init {
        Log.d(TAG, "init")
        if (itemId != null) {
            loadItem()
        } else {
            uiState = uiState.copy(loadResult = Result.Success(Item()))
        }
    }


    fun loadItem() {
        viewModelScope.launch {
            itemRepository.itemstream.collect { result ->
                if (!(uiState.loadResult is Result.Loading)) {
                    return@collect
                }
                if (result is Result.Success) {
                    val items = result.data
                    val game = items.find { it._id == itemId } ?: Item()
                    uiState = uiState.copy(loadResult = Result.Success(game), item = game)
                } else if (result is Result.Error) {
                    uiState =
                        uiState.copy(loadResult = Result.Error(result.exception))
                }
            }
        }
    }

    fun saveOrUpdateItem(
        name: String,
        description: String,
        noEmployees: Int,
        openingDateMillis: Long,
        isPublic: Boolean,
        photoPath: String?
    ) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateItem - started")
            try {
                uiState = uiState.copy(submitResult = Result.Loading)
                val openingDateString = DATE_FORMAT.format(Date(openingDateMillis))
                val itemToSave = uiState.item.copy(
                    name = name,
                    description = description,
                    noEmployees = noEmployees,
                    openingDate = openingDateString,
                    isPublic = isPublic,
                    photoPath = photoPath
                )
                val savedItem: Item =  itemRepository.update(itemToSave)

                Log.d(TAG, "saveOrUpdateItem - success")
                uiState = uiState.copy(submitResult = Result.Success(savedItem))
            } catch (e: Exception) {
                Log.d(TAG, "saveOrUpdateItem - error: ${e.message}")
                uiState = uiState.copy(submitResult = Result.Error(e))
            }
        }
    }

    fun AddItem(
        name: String,
        description: String,
        noEmployees: Int,
        openingDateMillis: Long,
        isPublic: Boolean,
        photoPath: String?
    ) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateItem - started")
            try {
                uiState = uiState.copy(submitResult = Result.Loading)
                val openingDateString = DATE_FORMAT.format(Date(openingDateMillis))
                val itemToSave = uiState.item.copy(
                    name = name,
                    description = description,
                    noEmployees = noEmployees,
                    openingDate = openingDateString,
                    isPublic = isPublic,
                    photoPath = photoPath
                )
                val savedItem: Item =  itemRepository.save(itemToSave)

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
