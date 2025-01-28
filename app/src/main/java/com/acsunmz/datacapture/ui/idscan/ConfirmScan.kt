package com.acsunmz.datacapture.ui.idscan

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.acsunmz.datacapture.core.data.SessionManager
import com.acsunmz.datacapture.core.model.Driver
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConfirmScan(
    onProceed: () -> Unit,
    documentData: Driver? = null
) {
    val context = LocalContext.current
    val driver = SessionManager.getDriver()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Driver Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dados do Condutor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    driver?.let {
                        DriverInfoItem("Nome", it.name)
                        DriverInfoItem("Número da Carta", it.licenceNumber)
                        DriverInfoItem("Número de Emissão", it.issueNumber.toString())
                        DriverInfoItem("Data de Nascimento", it.dateOfBirth.toFormattedDate())
                        DriverInfoItem("Validade", it.expiryDate.toFormattedDate())
                        DriverInfoItem("Local de Emissão", it.placeOfIssue)
                        DriverInfoItem("Gênero", it.gender)
                        DriverInfoItem("Restrições", it.restrictions)
                    } ?: Text("No driver data available")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Document Data Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dados do Documento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    documentData?.let {
                        DriverInfoItem("Nome", it.name)
                        DriverInfoItem("Número da Carta", it.licenceNumber)
                        // Add other document fields as needed
                    } ?: Text("No document data available")
                }
            }
        }

        // Proceed Button at Bottom
        Button(
            onClick = onProceed,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Continuar")
        }
    }
}

@Composable
private fun DriverInfoItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Extension function to format dates
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}


/*
@Composable
fun DriverDetailsScreen(
    onProceed: () -> Unit,
    documentData: Driver? = null
) {
    val context = LocalContext.current
    val driver = SessionManager.getDriver(context)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 72.dp) // Space for bottom button
        ) {
            // Driver Details Card
            DriverInfoCard(
                title = "Dados do Condutor",
                driver = driver,
                icon = Icons.Default.Person,
                iconTint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Document Data Card
            DriverInfoCard(
                title = "Dados do Documento",
                driver = documentData,
                icon = Icons.Default.Description,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }

        // Proceed Button at Bottom
        Button(
            onClick = onProceed,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Continuar")
        }
    }
}

@Composable
private fun DriverInfoCard(
    title: String,
    driver: Driver?,
    icon: ImageVector,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                driver?.let {
                    DriverDetailItem("Nome", it.name)
                    DriverDetailItem("Número da Carta", it.licenceNumber)
                    DriverDetailItem("Emissão", "#${it.issueNumber}")
                    DriverDetailItem("Nascimento", it.dateOfBirth.toFormattedDate())
                    DriverDetailItem("Validade", it.expiryDate.toFormattedDate())
                    DriverDetailItem("Local de Emissão", it.placeOfIssue)
                    DriverDetailItem("Gênero", it.gender)
                    DriverDetailItem("Restrições", it.restrictions)
                } ?: Text(
                    text = "Dados não disponíveis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DriverDetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Date formatting extension (keep previous implementation)
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}
 */
