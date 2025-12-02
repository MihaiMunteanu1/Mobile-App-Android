package com.example.myapplication.todo.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.myapplication.todo.data.remote.ItemService
import com.example.myapplication.todo.data.remote.ItemWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import com.example.myapplication.core.TAG
import com.example.myapplication.core.Result
import com.example.myapplication.todo.data.remote.ItemEvent2
import com.example.myapplication.core.data.remote.Api


class ItemRepo(private val itemService: ItemService, private val itemWsClient: ItemWsClient,
                     private val database: ItemDatabase, private val context: Context
) {
    private var items: List<Item> = listOf();


    private var itemsFlow: MutableSharedFlow<Result<List<Item>>> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val itemstream: Flow<Result<List<Item>>> = itemsFlow

    init {
        Log.d(TAG, "init")
    }

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            items = itemService.find(authorization = getBearerToken())
            database.itemDao().clear()
            for(Item in items) {
                Item.requiresUpdate = false
                Item.requiresCreate = false
                database.itemDao().insert(Item)
            }
            Log.d(TAG, "refresh succeeded")
            itemsFlow.emit(Result.Success(items))
        } catch (e: Exception) {
            Log.d(TAG, "refresh failed", e)
            items  =database.itemDao().getAll()
            itemsFlow.emit(Result.Success(items))
            //itemsFlow.emit(Result.Error(e))
        }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getItemEvents().collect {
                Log.d(TAG, "Item event collected $it")
                if (it is Result.Success) {
                    val itemEvent = it.data;
                    when (itemEvent.event) {
                        "created" -> handleItemCreated(itemEvent.payload.updatedItem)
                        "updated" -> handleItemUpdated(itemEvent.payload.updatedItem)
                        "deleted" -> handleItemDeleted(itemEvent.payload.updatedItem)
                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) {
            itemWsClient.closeSocket()
        }
    }

    suspend fun getItemEvents(): Flow<Result<ItemEvent2>> = callbackFlow {
        Log.d(TAG, "getItemEvents started")
        itemWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if (it != null) {
                    Log.d(TAG, "onEvent trySend $it")
                    trySend(Result.Success(it));
                }
            },
            onClosed = { close() },
            onFailure = { close() });
        awaitClose { itemWsClient.closeSocket() }
    }

    suspend fun update(Item: Item): Item {
        try {
            Item.requiresUpdate=false
            Log.d(TAG, "update $Item...")
            val updatedItem = itemService.update(authorization = getBearerToken(), Item._id, Item)
            Log.d(TAG, "update $Item succeeded")
            handleItemUpdated(updatedItem)
            return updatedItem
        }
        catch (ex:Exception){
            Log.d(TAG, "failed update $Item")
            Item.requiresUpdate=true
            handleItemUpdated(Item)

            Handler(Looper.getMainLooper()).post({
                Toast.makeText(context, "Server unreachable. Saved locally", Toast.LENGTH_LONG).show()
            })
            return Item
        }
    }

    suspend fun save(Item: Item): Item {
        try {
            Log.d(TAG, "save $Item...")
            Item.requiresCreate=false
            val createdItem = itemService.create(authorization = getBearerToken(), Item)
            Log.d(TAG, "save $Item succeeded")
            Log.d(TAG, "handle created $createdItem")
            handleItemCreated(createdItem)
            return createdItem
        }
        catch (ex:Exception){
            val createdItem = Item(
                name = Item.name,
                description = Item.description,
                openingDate = Item.openingDate,
                noEmployees = Item.noEmployees,
                isPublic = Item.isPublic,
                requiresCreate = true,
                requiresUpdate = false
            )
            Log.d(TAG, "failed create on the server $Item")
            handleItemCreated(createdItem)

            Handler(Looper.getMainLooper()).post({
                Toast.makeText(context, "Server unreachable. Saved locally", Toast.LENGTH_LONG).show()
            })
            return createdItem
        }
    }

    private suspend fun handleItemDeleted(Item: Item) {
        Log.d(TAG, "handleItemDeleted - todo $Item")
    }

    private suspend fun handleItemUpdated(Item: Item) {
        Log.d(TAG, "handleItemUpdated...: $Item")
        items = items.map { if (it._id == Item._id) Item else it }
        database.itemDao().update(Item)
        itemsFlow.emit(Result.Success(items))
    }

    private suspend fun handleItemCreated(Item: Item) {
        Log.d(TAG, "handleItemCreated...: $Item")
        if(!items.contains(Item)) {
            items = items.plus(Item)
            database.itemDao().insert(Item)
        }
        itemsFlow.emit(Result.Success(items))
    }

    fun quite_remove(Item:Item){
        items = items.minus(Item)
        database.itemDao().deleteById(Item._id)
    }

    fun setToken(token: String) {
        itemWsClient.authorize(token)
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"
}