package com.acsunmz.datacapture.feature.docscanner

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream

@Composable
fun DocumentScanner(
    onDocumentScanned: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(2)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()
    }

    val scanner = remember { GmsDocumentScanning.getClient(options) }

    var cartaAntigaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var atestadoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val cartaAntigaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            handleScanResult(activity, result.data, "carta_antiga.pdf").let {
                cartaAntigaUris = it.imageUris
            }
        }
    }

    val atestadoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            handleScanResult(activity, result.data, "atestado.pdf").let {
                atestadoUris = it.imageUris
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DocumentSection(
            title = "Carta Antiga",
            imageUris = cartaAntigaUris,
            onScanClick = { launchScanner(scanner, activity, cartaAntigaLauncher) }
        )

        DocumentSection(
            title = "Atestado",
            imageUris = atestadoUris,
            onScanClick = { launchScanner(scanner, activity, atestadoLauncher) }
        )

        // Continue button
        Button(
            onClick = onDocumentScanned,
            modifier = Modifier.fillMaxWidth(),
            enabled = cartaAntigaUris.isNotEmpty() && atestadoUris.isNotEmpty()
        ) {
            Text("Continuar")
        }
    }
}

@Composable  // Added composable annotation
private fun DocumentSection(
    title: String,
    imageUris: List<Uri>,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        imageUris.forEach { uri ->
            AsyncImage(
                model = uri,
                contentDescription = title,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            )
        }

        Button(onClick = onScanClick) {
            Text("Scan $title")
        }
    }
}

private fun handleScanResult(
    activity: Activity,  // Changed to Activity
    data: Intent?,
    fileName: String
): ScanResult {
    val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(data)
    val uris = scanResult?.pages?.map { it.imageUri } ?: emptyList()

    scanResult?.pdf?.let { pdf ->
        val outputFile = File(activity.filesDir, fileName)
        activity.contentResolver.openInputStream(pdf.uri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    return ScanResult(uris)
}

private fun launchScanner(
    scanner: GmsDocumentScanner,
    activity: Activity,  // Changed to Activity
    launcher: ActivityResultLauncher<IntentSenderRequest>
) {
    scanner.getStartScanIntent(activity)
        .addOnSuccessListener { intentSender ->
            launcher.launch(IntentSenderRequest.Builder(intentSender).build())
        }
        .addOnFailureListener { e ->
            Toast.makeText(activity, "Scan failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private data class ScanResult(val imageUris: List<Uri>)