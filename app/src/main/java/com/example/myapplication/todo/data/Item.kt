package com.example.myapplication.todo.data

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date


@Entity(tableName = "items")
data class Item(
    @PrimaryKey val _id: String = "",
    val name: String = "",
    val description: String = "",
    val noEmployees: Int = 0,
    val openingDate: String = convertDateToString(Date()),
    val isPublic: Boolean = false
)

@SuppressLint("SimpleDateFormat")
private fun convertDateToString(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy")
    return formatter.format(date)
}


