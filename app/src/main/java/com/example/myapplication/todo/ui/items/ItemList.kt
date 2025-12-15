
package com.example.myapplication.todo.ui.items

import android.util.Log
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.todo.data.Item
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
typealias OnItemFn = (id: String?) -> Unit

@Composable
fun ItemList(itemList: List<Item>, onItemClick: OnItemFn, modifier: Modifier) {
    Log.d("ItemList", "recompose")
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(itemList) { item ->
            ItemDetail(item, onItemClick)
        }
    }
}
@Composable
fun ItemDetail(item: Item, onItemClick: OnItemFn) {
    Log.d("ItemDetail", "recompose id = ${item._id}")
    Row(modifier = Modifier.padding(10.dp)) {
        val imageBitmap = remember(item.photoPath) {
            item.photoPath?.let { path ->
                BitmapFactory.decodeFile(path)?.asImageBitmap()
            }
        }
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Item photo",
                modifier = Modifier
                    .size(72.dp)
                    .padding(end = 12.dp)
            )
        }
        Column {
            if(item.requiresCreate){
                ClickableText(text = AnnotatedString(item.name),style = TextStyle(
                    fontSize = 24.sp,
                    color = Color.Blue,
                ), onClick = { onItemClick(item._id) })
            }
            else if(item.requiresUpdate){
                ClickableText(text = AnnotatedString(item.name),style = TextStyle(
                    fontSize = 24.sp,
                    color = Color.Green,
                ), onClick = { onItemClick(item._id) })
            }
            else{
                ClickableText(text = AnnotatedString(item.name),style = TextStyle(
                    fontSize = 24.sp,
                ), onClick = { onItemClick(item._id) })            }

            Text("Description: ${item.description}")
            Text("Employees: ${item.noEmployees}")
            Text("Opened: ${item.openingDate}")
            Text("Public: ${item.isPublic}")
        }
    }
}


