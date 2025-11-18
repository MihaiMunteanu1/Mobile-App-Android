package com.example.myapplication

import android.util.Log
import com.example.myapplication.todo.data.ItemRepository
import com.example.myapplication.todo.data.remote.ItemService
import com.example.myapplication.core.Api
import com.example.myapplication.core.TAG
import kotlin.jvm.java

class AppContainer {
    init {
        Log.d(TAG, "init")
    }

    val itemService: ItemService = Api.retrofit.create(ItemService::class.java)

    val itemRepository: ItemRepository by lazy {
        ItemRepository(itemService)
    }
}
