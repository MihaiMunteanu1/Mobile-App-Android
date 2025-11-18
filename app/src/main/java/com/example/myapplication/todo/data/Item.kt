package com.example.myapp.todo.data

import kotlinx.serialization.descriptors.PrimitiveKind
import java.util.Date

data class Item(val id: String, val name: String, val description: String, val noEmployees: Int,
    val openingDate: Date, val isPublic: Boolean)
