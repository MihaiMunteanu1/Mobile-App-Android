package com.example.myapp.todo.ui.item

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.todo.ui.item.ItemViewModel
import com.example.myapp.todo.ui.item.ItemViewModelFactory
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String, onClose: () -> Unit) {
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModelFactory(itemId))
    val itemUiState = itemViewModel.uiState
    val item = itemUiState.item

    var name by rememberSaveable { mutableStateOf(item?.name ?: "") }
    var description by rememberSaveable { mutableStateOf(item?.description ?: "") }
    var noEmployees by rememberSaveable { mutableStateOf(item?.noEmployees?.toString() ?: "1") }
    var isPublic by rememberSaveable { mutableStateOf(item?.isPublic ?: false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = item?.openingDate?.time,
        initialDisplayMode = DisplayMode.Picker
    )

    LaunchedEffect(item) {
        item?.let {
            name = it.name
            description = it.description
            noEmployees = it.noEmployees.toString()
            isPublic = it.isPublic
            datePickerState.selectedDateMillis = it.openingDate.time
        }
    }

    Log.d("ItemScreen", "recompose, name=$name")

    Column(modifier = Modifier.padding(8.dp)) {
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = noEmployees, onValueChange = { noEmployees = it }, label = { Text("No. Employees") })
        Spacer(modifier = Modifier.height(8.dp))

        Text("Opening Date")
        DatePicker(state = datePickerState, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Text("Public")
            Spacer(modifier = Modifier.height(4.dp))
            Switch(checked = isPublic, onCheckedChange = { isPublic = it })
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            Log.d("ItemScreen", "update item $itemId")
            val openingDateMillis = datePickerState.selectedDateMillis ?: Date().time
            itemViewModel.updateItem(name, description, noEmployees, openingDateMillis.toString(), isPublic)
            onClose()
        }) {
            Text("Update")
        }
    }
}
