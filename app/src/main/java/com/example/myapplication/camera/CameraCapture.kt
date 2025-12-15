//package com.example.myapplication.camera
//
//import android.Manifest
//import android.util.Log
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.camera.core.Preview
//import androidx.camera.core.UseCase
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.wrapContentSize
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.PermissionStatus
//import com.google.accompanist.permissions.rememberPermissionState
//import kotlinx.coroutines.launch
//import java.io.File
//import android.view.Surface
//
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun CameraCapture(
//    modifier: Modifier = Modifier,
//    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
//    onImageFile: (File) -> Unit = { },
//    onClose: () -> Unit = {}
//) {
//    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
//
//    when (cameraPermissionState.status) {
//        is PermissionStatus.Granted -> {
//            CameraCaptureContent(
//                modifier = modifier,
//                cameraSelector = cameraSelector,
//                onImageFile = onImageFile,
//                onClose = onClose
//            )
//        }
//        is PermissionStatus.Denied -> {
//            PermissionRationale(
//                modifier = modifier,
//                onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
//                onClose = onClose
//            )
//        }
//    }
//}
//
//@Composable
//private fun PermissionRationale(
//    modifier: Modifier = Modifier,
//    onRequestPermission: () -> Unit,
//    onClose: () -> Unit
//) {
//    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Button(onClick = onRequestPermission) {
//            Text(text = "Grant camera permission")
//        }
//        Button(
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(16.dp),
//            onClick = onClose
//        ) {
//            Text(text = "Close")
//        }
//    }
//}
//
//@Composable
//private fun CameraCaptureContent(
//    modifier: Modifier = Modifier,
//    cameraSelector: CameraSelector,
//    onImageFile: (File) -> Unit,
//    onClose: () -> Unit
//) {
//    Box(modifier = modifier) {
//        val context = LocalContext.current
//        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
//        val coroutineScope = rememberCoroutineScope()
//        var previewUseCase by remember { mutableStateOf<UseCase>(Preview.Builder().build()) }
//        val imageCaptureUseCase by remember {
//            mutableStateOf(
//                ImageCapture.Builder()
//                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
//                    .build()
//            )
//        }
//        Box {
//            CameraPreview(
//                modifier = Modifier.fillMaxSize(),
//                onUseCase = { useCase, rotation ->
//                    previewUseCase = useCase
//                    imageCaptureUseCase.targetRotation = rotation
//                }
//            )
//            Button(
//                modifier = Modifier
//                    .wrapContentSize()
//                    .padding(16.dp)
//                    .align(Alignment.BottomCenter),
//                onClick = {
//                    coroutineScope.launch {
//                        val photoFile = File(context.cacheDir, "item-photo-${System.currentTimeMillis()}.jpg")
//                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//                        imageCaptureUseCase.takePicture(
//                            outputOptions,
//                            ContextCompat.getMainExecutor(context),
//                            object : ImageCapture.OnImageSavedCallback {
//                                override fun onError(exception: ImageCaptureException) {
//                                    Log.e("CameraCapture", "Failed to save photo", exception)
//                                }
//
//                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                                    onImageFile(photoFile)
//                                }
//                            }
//                        )
//                    }
//                }
//            ) {
//                Text("Take photo")
//            }
//
//            Button(
//                modifier = Modifier
//                    .wrapContentSize()
//                    .padding(16.dp)
//                    .align(Alignment.TopEnd),
//                onClick = onClose
//            ) {
//                Text("Close")
//            }
//        }
//        LaunchedEffect(previewUseCase) {
//            val cameraProvider = context.getCameraProvider()
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(
//                    lifecycleOwner, cameraSelector, previewUseCase, imageCaptureUseCase
//                )
//            } catch (ex: Exception) {
//                Log.e("CameraCapture", "Failed to bind camera use cases", ex)
//            }
//        }
//    }
//}
//
//@Composable
//private fun CameraPreview(
//    modifier: Modifier = Modifier,
//    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
//    onUseCase: (UseCase, Int) -> Unit = { _, _ -> }
//) {
//    AndroidView(
//        modifier = modifier,
//        factory = { context ->
//            val previewView = PreviewView(context).apply {
//                this.scaleType = scaleType
//                layoutParams = android.view.ViewGroup.LayoutParams(
//                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
//                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
//                )
//            }
//            onUseCase(
//                Preview.Builder()
//                    .build()
//                    .also {
//                        it.setSurfaceProvider(previewView.surfaceProvider)
//                    },
//                previewView.display?.rotation ?: Surface.ROTATION_0
//            )
//            previewView
//        }
//    )
//}
package com.example.myapplication.camera

import android.Manifest
import android.util.Log
import android.view.OrientationEventListener
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.io.File
import android.view.Surface

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onImageFile: (File) -> Unit = { },
    onClose: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    when (cameraPermissionState.status) {
        is PermissionStatus.Granted -> {
            CameraCaptureContent(
                modifier = modifier,
                cameraSelector = cameraSelector,
                onImageFile = onImageFile,
                onClose = onClose
            )
        }
        is PermissionStatus.Denied -> {
            PermissionRationale(
                modifier = modifier,
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                onClose = onClose
            )
        }
    }
}

@Composable
private fun PermissionRationale(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onRequestPermission) {
            Text(text = "Grant camera permission")
        }
        Button(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            onClick = onClose
        ) {
            Text(text = "Close")
        }
    }
}

@Composable
private fun CameraCaptureContent(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector,
    onImageFile: (File) -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = modifier) {
        val context = LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        val coroutineScope = rememberCoroutineScope()
        var previewUseCase by remember { mutableStateOf<UseCase>(Preview.Builder().build()) }
        var imageCaptureRotation by remember { mutableStateOf(Surface.ROTATION_0) }
        val imageCaptureUseCase by remember {
            mutableStateOf(
                ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()
            )
        }
        DisposableEffect(Unit) {
            val orientationListener = object : OrientationEventListener(context) {
                override fun onOrientationChanged(orientation: Int) {
                    val rotation = when (orientation) {
                        in 45..134 -> Surface.ROTATION_270
                        in 135..224 -> Surface.ROTATION_180
                        in 225..314 -> Surface.ROTATION_90
                        else -> Surface.ROTATION_0
                    }
                    if (rotation != imageCaptureRotation) {
                        imageCaptureRotation = rotation
                        imageCaptureUseCase.targetRotation = rotation
                    }
                }
            }
            orientationListener.enable()
            onDispose { orientationListener.disable() }
        }
        Box {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onUseCase = { useCase, rotation ->
                    previewUseCase = useCase
                    imageCaptureUseCase.targetRotation = rotation
                    imageCaptureRotation = rotation
                }
            )
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                onClick = {
                    coroutineScope.launch {
                        val photoFile = File(context.cacheDir, "item-photo-${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                        imageCaptureUseCase.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("CameraCapture", "Failed to save photo", exception)
                                }

                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    onImageFile(photoFile)
                                }
                            }
                        )
                    }
                }
            ) {
                Text("Take photo")
            }

            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
                onClick = onClose
            ) {
                Text("Close")
            }
        }
        LaunchedEffect(previewUseCase) {
            val cameraProvider = context.getCameraProvider()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, previewUseCase, imageCaptureUseCase
                )
            } catch (ex: Exception) {
                Log.e("CameraCapture", "Failed to bind camera use cases", ex)
            }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    onUseCase: (UseCase, Int) -> Unit = { _, _ -> }
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = scaleType
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            onUseCase(
                Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    },
                previewView.display?.rotation ?: Surface.ROTATION_0
            )
            previewView
        }
    )
}