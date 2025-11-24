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
    ){
    // Funcție helper pentru a converti string-ul din `item` în milisecunde pentru DatePicker
    val openingDateMillis: Long
        get() {
            return try {
                DATE_FORMAT.parse(item.openingDate)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
}

class ItemViewModel(private val itemId: String?, private val itemRepository: ItemRepository) : ViewModel() {

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
            itemRepository.itemStream.collect { items ->
                if (!(uiState.loadResult is Result.Loading)) {
                    return@collect
                }
                val item = items.find { it._id == itemId } ?: Item()
                uiState = uiState.copy(item = item, loadResult = Result.Success(item))
            }
        }
    }

    fun saveOrUpdateItem(
        name: String,
        description: String,
        noEmployees: Int,
        openingDateMillis: Long,
        isPublic: Boolean
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
                    isPublic = isPublic
                )

                val savedItem = if (itemId == null) {
                    itemRepository.save(itemToSave)
                } else {
                    //itemRepository.update(itemToSave)
                    itemRepository.update(itemId, itemToSave.copy(_id = itemId))

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
