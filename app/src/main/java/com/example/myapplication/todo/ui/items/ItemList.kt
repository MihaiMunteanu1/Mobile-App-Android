// app/src/main/java/com/example/myapplication/todo/ui/items/ItemList.kt
package com.example.myapp.todo.ui.items

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.myapp.todo.data.Item

typealias OnItemFn = (id: String) -> Unit

@Composable
fun ItemList(itemList: List<Item>, onItemClick: OnItemFn) {
    Log.d("ItemList", "recompose")
    LazyColumn {
        items(itemList) { item ->
            ItemDetail(item, onItemClick)
        }
    }
}
@Composable
fun ItemDetail(item: Item, onItemClick: OnItemFn) {
    Log.d("ItemDetail", "recompose id = ${item.id}")
    Row(modifier = Modifier.padding(10.dp)) {
        Column {
            ClickableText(text = AnnotatedString(item.name), onClick = { onItemClick(item.id) })
            Text("Description: ${item.description}")
            Text("Employees: ${item.noEmployees}")
            Text("Opened: ${item.openingDate}")
            Text("Public: ${item.isPublic}")
        }
    }
}

//@Composable
//fun ItemDetail(item: Item, onItemClick: OnItemFn) {
//    Log.d("ItemDetail", "recompose id = ${item.id}")
//    Row {
//        Column {
//            ClickableText(text = AnnotatedString(item.name), onClick = { onItemClick(item.id) })
//            AnnotatedString("Description: ${item.description}")
//            AnnotatedString("Employees: ${item.noEmployees}")
//            AnnotatedString("Opened: ${item.openingDate}")
//            AnnotatedString("Public: ${item.isPublic}")
//        }
//    }
//}
