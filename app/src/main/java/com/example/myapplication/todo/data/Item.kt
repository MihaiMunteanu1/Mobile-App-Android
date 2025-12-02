package com.example.myapplication.todo.data

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date


@Entity(tableName = "items")
data class Item(
    @PrimaryKey
    @SerializedName("_id")
    val _id: String = "${System.currentTimeMillis()*10000}",
    val name: String = "",
    val description: String = "",
    val noEmployees: Int = 0,
    val openingDate: String = convertDateToString(Date()),
    val isPublic: Boolean = false,

    var requiresCreate: Boolean=false,
    var requiresUpdate: Boolean=false
)

@SuppressLint("SimpleDateFormat")
private fun convertDateToString(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy")
    return formatter.format(date)
}


