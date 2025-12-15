package com.example.myapplication.todo.ui.item

import android.R.attr.timeZone
import android.util.Log
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import com.example.myapplication.camera.CameraCapture
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import java.io.File
import java.io.IOException
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

    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            copyImageFromUri(context, it)?.let { savedPath ->
                photoPath = savedPath
            }
        }
    }

    val loadResult = itemUiState.loadResult

    val item = if (loadResult is Result.Success) {
        loadResult.data
    } else {
        null
    }


    val dateFormat = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    val datePickerState = remember(item) {
        val initialMillis = if (item != null) {
            try {
                dateFormat.parse(item.openingDate)?.time
            } catch (e: Exception) {
                Log.e("ItemAddScreen", "Failed to parse date: ${item.openingDate}", e)
                System.currentTimeMillis()
            }
        } else {
            System.currentTimeMillis()
        }

        DatePickerState(
            initialSelectedDateMillis = initialMillis ?: System.currentTimeMillis(),
            locale = Locale.getDefault()
        )
    }

    Log.d("ItemScreen2", "recompose, name = $name")

    LaunchedEffect(item) {
        if (item != null) {
            Log.d("ItemAddScreen", "Updating UI state with item: ${item.name}")
            name = item.name
            description = item.description
            noEmployees = item.noEmployees.toString()
            isPublic = item.isPublic
            photoPath = item.photoPath
        }
    }

    LaunchedEffect(itemUiState.submitResult) {
        Log.d("ItemScreen2", "Submit = ${itemUiState.submitResult}")
        if (itemUiState.submitResult is Result.Success) {
            Log.d("ItemScreen2", "Closing screen")
            onClose()
        }
    }

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
                            isPublic,
                            photoPath
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
                .verticalScroll(rememberScrollState())
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

                photoPath?.let { path ->
                    val bitmap = remember(path) { BitmapFactory.decodeFile(path)?.asImageBitmap() }
                    bitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Item photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .aspectRatio(4f / 3f),
                            contentScale = Crop
//                            bitmap = it,
//                            contentDescription = "Item photo",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp)
//                                .padding(vertical = 8.dp)
                        )
                    }
                }
                if (showCamera) {
                    Dialog(onDismissRequest = { showCamera = false }) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                        ) {
                            CameraCapture(
                                modifier = Modifier.fillMaxSize(),
                                onImageFile = { file ->
                                    photoPath = file.absolutePath
                                    showCamera = false
                                },
                                onClose = { showCamera = false }
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { showCamera = true }) {
                            Text("Camera")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { galleryLauncher.launch("image/*") }) {
                            Text("Gallery")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (photoPath != null) {
                            Button(onClick = { photoPath = null }) {
                                Text("Remove")
                            }
                        }
                    }
                }
//                if (showCamera) {
//                    CameraCapture(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp),
//                        onImageFile = { file ->
//                            photoPath = file.absolutePath
//                            showCamera = false
//                        },
//                        onClose = { showCamera = false }
//                    )
//                }

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
private fun copyImageFromUri(context: Context, uri: Uri): String? {
    return try {
        val photoFile = File(context.cacheDir, "item-photo-${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            photoFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: return null
        photoFile.absolutePath
    } catch (e: IOException) {
        Log.e("ItemAddScreen", "Failed to copy image from gallery", e)
        null
    }
}