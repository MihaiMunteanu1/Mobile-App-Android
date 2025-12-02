package com.example.myapplication.todo.ui.item

import android.R.attr.timeZone
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.setSelection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.core.Result
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemAddScreen(itemId: String?, onClose: () -> Unit) {
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModel.Factory(itemId))
    val itemUiState = itemViewModel.uiState

    // Initialize state from ViewModel's uiState
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var noEmployees by rememberSaveable { mutableStateOf("") }
    var isPublic by rememberSaveable { mutableStateOf(false) }
    //val datePickerState = rememberDatePickerState(initialSelectedDateMillis = itemUiState.item.openingDate.time)
    //var datePickerState by rememberSaveable {mutableStateOf(itemUiState.item.openingDate)}


    val loadResult = itemUiState.loadResult

    val item = if (loadResult is Result.Success) {
        // Acum lucrezi cu variabila locală 'loadResult', care nu se poate schimba
        // Smart cast-ul este sigur aici.
        loadResult.data
    } else {
        null
    }

//    val dateFormat = remember {
//        SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
//            timeZone = TimeZone.getTimeZone("UTC")
//        }
//    }

//    val datePickerState = rememberDatePickerState(
//        initialSelectedDateMillis = System.currentTimeMillis()
//    )

    val dateFormat = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    val datePickerState = remember(item) {
        // Calculează milisecundele o singură dată, la inițializare
        val initialMillis = if (item != null) {
            try {
                // Încearcă să parsezi String-ul într-un obiect Date, apoi ia milisecundele
                dateFormat.parse(item.openingDate)?.time
            } catch (e: Exception) {
                // Dacă parsarea eșuează, folosește data curentă ca alternativă
                Log.e("ItemAddScreen", "Failed to parse date: ${item.openingDate}", e)
                System.currentTimeMillis()
            }
        } else {
            // Dacă nu există item (mod adăugare), folosește data curentă
            System.currentTimeMillis()
        }

        DatePickerState(
            initialSelectedDateMillis = initialMillis ?: System.currentTimeMillis(),
            locale = Locale.getDefault()
        )
    }

    Log.d("ItemScreen2", "recompose, name = $name")

    // 3. ADAUGĂ ACEST BLOC NOU: Sincronizează starea UI cu ViewModel
    LaunchedEffect(item) {
        if (item != null) {
            Log.d("ItemAddScreen", "Updating UI state with item: ${item.name}")
            name = item.name
            description = item.description
            noEmployees = item.noEmployees.toString()
            isPublic = item.isPublic
        }
    }

    // Effect to handle closing the screen on successful submission
    LaunchedEffect(itemUiState.submitResult) {
        Log.d("ItemScreen2", "Submit = ${itemUiState.submitResult}")
        if (itemUiState.submitResult is Result.Success) {
            Log.d("ItemScreen2", "Closing screen")
            onClose()
        }
    }

//    // Effect to handle closing the screen on successful submission
//    LaunchedEffect(itemUiState.submitResult) {
//        Log.d("ItemScreen2", "Submit = ${itemUiState.submitResult}")
//        if (itemUiState.submitResult is Result.Success) {
//            Log.d("ItemScreen2", "Closing screen")
//            onClose()
//        }
//    }
//
//    // A flag to prevent re-initializing the state on every recomposition
//    var uiStateInitialized by remember { mutableStateOf(itemId == null) }
//
//    // Effect to initialize the screen's state from the ViewModel once data is loaded
//    LaunchedEffect(itemId, itemUiState.loadResult) {
//        Log.d("ItemScreen2", "LoadResult changed: ${itemUiState.loadResult}")
//        if (uiStateInitialized) {
//            return@LaunchedEffect
//        }
//        if (itemUiState.loadResult !is Result.Loading) {
//            Log.d("ItemScreen2", "Initializing state from loaded item: ${itemUiState.item}")
//            name = itemUiState.item.name
//            description = itemUiState.item.description
//            noEmployees = itemUiState.item.noEmployees.toString()
//            isPublic = itemUiState.item.isPublic
//            val dateMillis = try {
//                dateFormat.parse(itemUiState.item.openingDate)?.time
//            } catch (e: Exception) {
//                null
//            }
//            dateMillis?.let { datePickerState.selectedDateMillis = it }
//
//            uiStateInitialized = true
//        }
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.item)) },
                actions = {
                    Button(onClick = {
                        Log.d("ItemScreen2", "Save/Update item button clicked")
                        val openingDateMillis = datePickerState.selectedDateMillis ?: Date().time
                        itemViewModel.AddItem(
                            name,
                            description,
                            noEmployees.toIntOrNull() ?: 0,
                            openingDateMillis,
                            isPublic
                        )
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Show loading indicator when the item is being loaded
            if (itemUiState.loadResult is Result.Loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Show submission progress indicator
                if (itemUiState.submitResult is Result.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // Show error message if loading failed
                if (itemUiState.loadResult is Result.Error) {
                    Text(text = "Failed to load item - ${(itemUiState.loadResult as Result.Error).exception?.message}")
                    Spacer(modifier = Modifier.height(16.dp))
                }

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = itemUiState.loadResult !is Result.Loading
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = itemUiState.loadResult !is Result.Loading
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = noEmployees,
                    onValueChange = { new -> noEmployees = new.filter { it.isDigit() } },
                    label = { Text("No. Employees") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = itemUiState.loadResult !is Result.Loading
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Opening Date")
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.padding(top = 4.dp),
                    // The DatePicker itself does not have an 'enabled' parameter in this version.
                    // The parent container could be disabled if needed.
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Public")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        enabled = itemUiState.loadResult !is Result.Loading
                    )
                }

                // Show error message if submission failed
                if (itemUiState.submitResult is Result.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Failed to submit item: ${(itemUiState.submitResult as Result.Error).exception?.message}",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
