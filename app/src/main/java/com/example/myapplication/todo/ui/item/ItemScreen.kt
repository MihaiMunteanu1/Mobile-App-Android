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
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import java.io.File
import java.io.IOException
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog

import com.example.myapplication.camera.CameraCapture
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String?, onClose: () -> Unit) {
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModel.Factory(itemId))
    val itemUiState = itemViewModel.uiState

    // Initialize state from ViewModel's uiState
    var name by rememberSaveable { mutableStateOf(itemUiState.item.name) }
    var description by rememberSaveable { mutableStateOf(itemUiState.item.description) }
    var noEmployees by rememberSaveable { mutableStateOf(itemUiState.item.noEmployees.toString()) }
    var isPublic by rememberSaveable { mutableStateOf(itemUiState.item.isPublic) }
    //val datePickerState = rememberDatePickerState(initialSelectedDateMillis = itemUiState.item.openingDate.time)
    //var datePickerState by rememberSaveable {mutableStateOf(itemUiState.item.openingDate)}
    var photoPath by rememberSaveable { mutableStateOf(itemUiState.item.photoPath) }
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

    val dateFormat = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            dateFormat.parse(itemUiState.item.openingDate)?.time
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )


    Log.d("ItemScreen", "recompose, name = $name")

    // Effect to handle closing the screen on successful submission
    LaunchedEffect(itemUiState.submitResult) {
        Log.d("ItemScreen", "Submit = ${itemUiState.submitResult}")
        if (itemUiState.submitResult is Result.Success) {
            Log.d("ItemScreen", "Closing screen")
            onClose()
        }
    }

    // A flag to prevent re-initializing the state on every recomposition
    var uiStateInitialized by remember { mutableStateOf(itemId == null) }

    // Effect to initialize the screen's state from the ViewModel once data is loaded
    LaunchedEffect(itemId, itemUiState.loadResult) {
        Log.d("ItemScreen2", "LoadResult changed: ${itemUiState.loadResult}")
        if (uiStateInitialized) {
            return@LaunchedEffect
        }
        if (itemUiState.loadResult !is Result.Loading) {
            Log.d("ItemScreen2", "Initializing state from loaded item: ${itemUiState.item}")
            name = itemUiState.item.name
            description = itemUiState.item.description
            noEmployees = itemUiState.item.noEmployees.toString()
            isPublic = itemUiState.item.isPublic
            photoPath = itemUiState.item.photoPath
            val dateMillis = try {
                dateFormat.parse(itemUiState.item.openingDate)?.time
            } catch (e: Exception) {
                null
            }
            dateMillis?.let { datePickerState.selectedDateMillis = it }

            uiStateInitialized = true
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
                        itemViewModel.saveOrUpdateItem(
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
                Spacer(modifier = Modifier.height(16.dp))
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
                            contentScale = ContentScale.Crop
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
                }
//                if (showCamera) {
//                    CameraCapture(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(50.dp),
//                        onImageFile = { file ->
//                            photoPath = file.absolutePath
//                            showCamera = false
//                        },
//                        onClose = { showCamera = false }
//                    )
                else {
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
        Log.e("ItemScreen", "Failed to copy image from gallery", e)
        null
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewItemScreen() {
    ItemScreen(itemId = null, onClose = {})
}
