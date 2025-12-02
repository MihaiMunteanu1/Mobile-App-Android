package com.example.myapplication.todo.data.remote

import com.example.myapplication.todo.data.Item

data class Payload(val updatedItem: Item)
data class ItemEvent2(val event: String, val payload: Payload)
