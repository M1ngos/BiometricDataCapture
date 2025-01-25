package com.acsunmz.datacapture.ui.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.acsunmz.datacapture.core.model.IdData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdConfirmationScreen(
    idData: IdData,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Confirmação dos dados") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onEdit,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text("Edit")
                        }
                        FilledTonalButton(onClick = onConfirm) {
                            Text("Confirm Details")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Personal Information Section
            InfoSection(
                title = "Personal Information",
                items = listOf(
                    "Full Name" to idData.fullName,
                    "Date of Birth" to idData.dateOfBirth,
                    "Sex" to idData.sex,
                    "Height" to idData.height,
                    "Place of Birth" to idData.placeOfBirth,
                    "Address" to idData.address
                )
            )

            // Document Details Section
            InfoSection(
                title = "Document Details",
                items = listOf(
                    "ID Number" to idData.idNumber,
                    "Issuance Date" to idData.issuanceDate,
                    "Expiry Date" to idData.expiryDate
                )
            )

            // Family Information Section
            InfoSection(
                title = "Family Information",
                items = listOf(
                    "Marital Status" to idData.maritalStatus,
                    "Father's Name" to idData.fatherName,
                    "Mother's Name" to idData.motherName
                )
            )
        }
    }
}

@Composable
private fun InfoSection(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            items.forEach { (label, value) ->
                if (value.isNotEmpty()) {
                    InfoRow(label = label, value = value)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 2
        )
    }
}