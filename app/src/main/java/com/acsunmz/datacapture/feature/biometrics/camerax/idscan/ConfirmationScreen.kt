package com.acsunmz.datacapture.feature.biometrics.camerax.idscan

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ConfirmationScreen(
    documentType: DocumentType,
    extractedData: Map<String, String>,
    onConfirm: (Map<String, String>) -> Unit
) {
    var editedData by remember { mutableStateOf(extractedData.toMutableMap()) }
    var hasErrors by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Confirm ${documentType.title} Details",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        documentType.fields.forEach { field ->
            if (field.name != "Photo Location") {
                OutlinedTextField(
                    value = editedData[field.name] ?: "",
                    onValueChange = { value ->
                        editedData[field.name] = value
                    },
                    label = { Text(field.name) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    isError = field.required && (editedData[field.name].isNullOrBlank() ||
                            (field.regex != null && !Regex(field.regex).matches(editedData[field.name] ?: "")))
                )
            }
        }

        // Display extracted photo if available
        extractedData["Photo Location"]?.let { photoLocation ->
            // In a real app, you would display the cropped photo here
            Text(
                text = "Photo detected",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                hasErrors = documentType.fields.any { field ->
                    field.required && (editedData[field.name].isNullOrBlank() ||
                            (field.regex != null && !Regex(field.regex).matches(editedData[field.name] ?: "")))
                }

                if (!hasErrors) {
                    onConfirm(editedData)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm")
        }

        if (hasErrors) {
            Text(
                text = "Please fix the errors before confirming",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}