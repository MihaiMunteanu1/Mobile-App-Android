
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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*

typealias OnItemFn = (id: String?) -> Unit
@Composable
fun ItemList(itemList: List<Item>, onItemClick: OnItemFn, modifier: Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        itemsIndexed(
            items = itemList,
            key = { _, item -> item._id ?: item.hashCode() } // important pt state per item
        ) { index, item ->
            FadeInItem(
                itemId = item._id,
                index = index,
                stepMillis = 550L,      // 2 sec între item-uri
                durationMillis = 600     // cât durează fade-ul
            ) {
                ItemDetail(item, onItemClick)
            }
        }
    }
}

@Composable
fun ItemDetail(item: Item, onItemClick: OnItemFn) {
    Log.d("ItemDetail", "recompose id = ${item._id}")
    Row(
        modifier = Modifier
            .padding(10.dp)
            .animateContentSize()
    ) {
        var isExpanded by rememberSaveable(item._id) { mutableStateOf(false) }
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
                    .clickable { isExpanded = !isExpanded }
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

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "More details: expandable content shown here...",
                    style = TextStyle(fontSize = 16.sp)
                )
            }
        }
    }
}

@Composable
private fun FadeInItem(
    itemId: String?,
    index: Int,
    stepMillis: Long,
    durationMillis: Int,
    content: @Composable () -> Unit
) {
    var visible by rememberSaveable(itemId) { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        if (!visible) {
            kotlinx.coroutines.delay(index * stepMillis) // stagger: 0s, 2s, 4s...
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = durationMillis))
    ) {
        content()
    }
}
