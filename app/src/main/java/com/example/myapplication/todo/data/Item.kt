package com.example.myapplication.todo.data

import kotlinx.serialization.descriptors.PrimitiveKind
import java.util.Date

data class Item(val id: String? = null, val name: String = "", val description: String = "", val noEmployees: Int = 0,
    val openingDate: Date = Date(), val isPublic: Boolean = false)
