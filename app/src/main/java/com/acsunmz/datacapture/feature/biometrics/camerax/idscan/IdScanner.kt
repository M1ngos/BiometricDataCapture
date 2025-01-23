import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acsunmz.datacapture.R
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.kotlinx.serializer.KotlinxSerializer
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.util.InternalAPI
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.json

@OptIn(ExperimentalGetImage::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun IdScanner(
    viewModel: IdScannerViewModel = viewModel(),
    onScanComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // State for tracking which side of ID is being captured
    var isCapturingFrontSide by remember { mutableStateOf(true) }
    var frontImageFile by remember { mutableStateOf<File?>(null) }

    // Camera state
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // Camera Preview
            Box(modifier = Modifier.fillMaxWidth()) {
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context).apply {
                            this.scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                        cameraProviderFuture.addListener({
                            cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build()
                            imageCaptureUseCase = ImageCapture.Builder()
                                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                                .build()

                            try {
                                cameraProvider?.unbindAll()
                                cameraProvider?.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCaptureUseCase
                                )

                                preview.setSurfaceProvider(previewView.surfaceProvider)
                            } catch (e: Exception) {
                                Log.e("IdScanner", "Camera initialization failed", e)
                            }
                        }, ContextCompat.getMainExecutor(context))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay with scanning guide
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top section with instructions
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isCapturingFrontSide) "Scan Front of ID" else "Scan Back of ID",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tips for best results:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "• Ensure good lighting\n• Avoid glare and shadows\n• Keep ID within frame\n• Hold steady",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Bottom section with capture button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                coroutineScope.launch {
                                    val file = File(
                                        context.cacheDir,
                                        if (isCapturingFrontSide) "id_front.jpg" else "id_back.jpg"
                                    )

                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                                    imageCaptureUseCase?.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                if (isCapturingFrontSide) {
                                                    frontImageFile = file
                                                    isCapturingFrontSide = false
                                                } else {
                                                    // Both sides captured, upload images
                                                    coroutineScope.launch {
                                                        frontImageFile?.let { frontFile ->
                                                            viewModel.uploadIdImages(frontFile, file)
                                                            onScanComplete()
                                                        }
                                                    }
                                                }
                                            }

                                            override fun onError(exc: ImageCaptureException) {
                                                Log.e("IdScanner", "Failed to capture image", exc)
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.Center),
                            shape = CircleShape,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.photo_camera_24dp),
                                contentDescription = "Capture ID",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// ViewModel for handling ID scanning logic and API calls
class IdScannerViewModel : ViewModel() {
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    //TODO: CHANGE TO GENERIC FROM CAMERAVIEWMODEL
    @OptIn(InternalAPI::class)
    suspend fun uploadIdImages(frontImage: File, backImage: File) {
        try {
            val multipartData = MultiPartFormDataContent(
                formData {
                    append("front_image", frontImage.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=front.jpg")
                    })
                    append("back_image", backImage.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=back.jpg")
                    })
                }
            )

            httpClient.post("http://192.168.1.144:8000/extract-data") {
                body = multipartData
            }
        } catch (e: Exception) {
            Log.e("IdScannerViewModel", "Failed to upload images", e)
            throw e
        }
    }
}