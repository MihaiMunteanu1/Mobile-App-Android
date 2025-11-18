package com.example.myapp.todo.data

import android.util.Log
import java.time.LocalDate
import java.util.Date

object ItemRepository {
    private val _items = List(100) { index ->
        Item(
            id = "$index",
            name = "Item $index",
            description = "Description for item $index",
            noEmployees = (index % 10) + 1,
            openingDate = Date.from(
                LocalDate.now()
                    .minusDays(index.toLong())
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant()
            ),            isPublic = index % 2 == 0
        )
    }.toMutableList()

    val items: List<Item> = _items

    fun update(
        id: String,
        name: String,
        description: String,
        noEmployees: Int,
        openingDate: Date,
        isPublic: Boolean
    ): Item? {
        Log.d("ItemRepository", "update $id")
        val index = _items.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = _items[index].copy(
                name = name,
                description = description,
                noEmployees = noEmployees,
                openingDate = openingDate,
                isPublic = isPublic
            )
            _items[index] = item
            return item
        }
        return null
    }
}
