package com.example.myapplication.core

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.MyAppDatabase
import com.example.myapplication.auth.data.AuthRepository
import com.example.myapplication.auth.data.remote.AuthDataSource
import com.example.myapplication.core.data.UserPreferencesRepository
import com.example.myapplication.core.data.remote.Api
import com.example.myapplication.todo.data.ItemRepository
import com.example.myapplication.todo.data.remote.ItemService
import com.example.myapplication.todo.data.remote.ItemWsClient
import kotlin.jvm.java

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    init {
        Log.d(TAG, "init")
    }

    private val itemService: ItemService = Api.retrofit.create(ItemService::class.java)
    private val itemWsClient: ItemWsClient = ItemWsClient(Api.okHttpClient)
    private val authDataSource: AuthDataSource = AuthDataSource()

    private val database: MyAppDatabase by lazy { MyAppDatabase.getDatabase(context) }

    val itemRepository: ItemRepository by lazy {
        ItemRepository(itemService, itemWsClient, database.itemDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authDataSource)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.userPreferencesDataStore)
    }
}
