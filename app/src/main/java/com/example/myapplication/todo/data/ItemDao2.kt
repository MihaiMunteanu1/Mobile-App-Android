package com.example.myapplication.todo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDao2 {
    @Query("SELECT * FROM items")
    fun getAll(): List<Item>

    @Query("SELECT * FROM items WHERE _id == :id")
    fun getById(id: String): List<Item>

    @Insert
    fun insert(item: Item)

    @Update
    fun update(item: Item)

    @Query("DELETE FROM items")
    fun clear()

    @Query("DELETE FROM items WHERE _id == :id")
    fun deleteById(id:String)
}