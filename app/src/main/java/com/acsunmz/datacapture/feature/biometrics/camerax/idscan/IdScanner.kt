//import android.Manifest
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import com.chaquo.python.Python
//import com.chaquo.python.android.AndroidPlatform
//import java.util.concurrent.Executors
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.annotation.OptIn
//import androidx.camera.core.ImageCapture.OnImageCapturedCallback
//import androidx.camera.core.ImageProxy
//import java.nio.ByteBuffer
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.io.ByteArrayOutputStream
//
//data class ExtractedData(
//    val success: Boolean,
//    val data: Map<String, String>,
//    val error: String?
//)
//
//@Suppress("UNCHECKED_CAST")
//private fun convertPythonResult(result: Any): ExtractedData {
//    val resultMap = result as java.util.HashMap<String, Any>
//    return ExtractedData(
//        success = resultMap["success"] as Boolean,
//        data = (resultMap["data"] as java.util.HashMap<String, String>).toMap(),
//        error = resultMap["error"] as? String
//    )
//}
//
//
//@Composable
//fun IdScanner(
//    onDataExtracted: (ExtractedData) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val scope = rememberCoroutineScope()
//
//    var extractedData by remember { mutableStateOf<ExtractedData?>(null) }
//    var isProcessing by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
//    val imageCapture = remember {
//        ImageCapture.Builder()
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//            .build()
//    }
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (!isGranted) {
//            // Handle permission denial
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        permissionLauncher.launch(Manifest.permission.CAMERA)
//    }
//
//    LaunchedEffect(Unit) {
//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(context))
//        }
//    }
//
//    Column(
//        modifier = modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxWidth()
//        ) {
//            AndroidView(
//                factory = { context ->
//                    PreviewView(context).apply {
//                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
//
//                        cameraProviderFuture.addListener({
//                            val cameraProvider = cameraProviderFuture.get()
//                            val preview = Preview.Builder().build()
//                            preview.setSurfaceProvider(this.surfaceProvider)
//                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//                            try {
//                                cameraProvider.unbindAll()
//                                cameraProvider.bindToLifecycle(
//                                    lifecycleOwner,
//                                    cameraSelector,
//                                    preview,
//                                    imageCapture
//                                )
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }, ContextCompat.getMainExecutor(context))
//                    }
//                },
//                modifier = Modifier.fillMaxSize()
//            )
//
//            Box(
//                modifier = Modifier
//                    .size(300.dp, 200.dp)
//                    .align(Alignment.Center)
//                    .border(2.dp, Color.White)
//            )
//        }
//
//        Button(
//            onClick = {
//                if (!isProcessing) {
//                    isProcessing = true
//                    errorMessage = null
//
//                    imageCapture.takePicture(
//                        cameraExecutor,
//                        object : OnImageCapturedCallback() {
//                            override fun onCaptureSuccess(image: ImageProxy) {
//                                scope.launch {
//                                    try {
//                                        val bitmap = image.toBitmap()
//                                        val result = processImage(bitmap)
//                                        onDataExtracted(result)
//                                    } catch (e: Exception) {
//                                        onDataExtracted(
//                                            ExtractedData(
//                                                success = false,
//                                                data = emptyMap(),
//                                                error = e.message
//                                            )
//                                        )
//                                    } finally {
//                                        isProcessing = false
//                                        image.close()
//                                    }
//                                }
//                            }
//
//                            override fun onError(exception: ImageCaptureException) {
//                                onDataExtracted(
//                                    ExtractedData(
//                                        success = false,
//                                        data = emptyMap(),
//                                        error = exception.message
//                                    )
//                                )
//                                isProcessing = false
//                            }
//                        }
//                    )
//                }
//            },
//            modifier = Modifier.padding(16.dp),
//            enabled = !isProcessing
//        ) {
//            Text(if (isProcessing) "Processing..." else "Capture ID")
//        }
//
//        errorMessage?.let {
//            Text(
//                text = it,
//                color = MaterialTheme.colorScheme.error,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            cameraExecutor.shutdown()
//        }
//    }
//}
//
//private suspend fun processImage(bitmap: Bitmap): ExtractedData {
//    return withContext(Dispatchers.IO) {
//        try {
//            val python = Python.getInstance()
//            val extractorModule = python.getModule("id_data_extractor")
//            val extractor = extractorModule.callAttr("IDDataExtractor")
////
//
//            val stream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//            val byteArray = stream.toByteArray()
//
//            val result = extractor.callAttr("extract_data", byteArray)
//            convertPythonResult(result.toJava(Any::class.java))
//        } catch (e: Exception) {
//            ExtractedData(
//                success = false,
//                data = emptyMap(),
//                error = e.message
//            )
//        }
//    }
//}
//
//// Extension function to convert ImageProxy to Bitmap
//@OptIn(ExperimentalGetImage::class)
//private fun ImageProxy.toBitmap(): Bitmap {
//    val buffer: ByteBuffer = image?.planes?.get(0)?.buffer ?: throw Exception("Failed to get image buffer")
//    val bytes = ByteArray(buffer.remaining())
//    buffer.get(bytes)
//    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//}