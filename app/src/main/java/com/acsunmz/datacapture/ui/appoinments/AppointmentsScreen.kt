package com.acsunmz.datacapture.ui.appoinments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import androidx.compose.runtime.*
import com.acsunmz.datacapture.core.data.SessionManager
import com.acsunmz.datacapture.core.model.Appointment
import com.acsunmz.datacapture.core.model.AppointmentStatus
import com.acsunmz.datacapture.core.model.AppointmentType
import com.acsunmz.datacapture.core.model.Driver
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    onLogout: () -> Unit,
) {
    val driver = SessionManager.getDriver()
    // Use the driver info (display it or perform logic)
//    println("Driver Name: ${driver.name}")
//    Log.d("Getting driver",driver.licenseId)

    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
//                appointments = apiService.getAppointments(token)
            } catch (e: Exception) {
                Log.e("AppointmentsScreen", "Error fetching appointments", e)
            }
        }
    }

    if (driver != null) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Minhas Marcações",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                DriverInfoCard(
                    driver = driver,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                if (appointments.isEmpty()) {
                    EmptyAppointmentsView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    AppointmentsList(
                        appointments = appointments,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}


@Composable
private fun DriverInfoCard(
    driver: Driver,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = driver.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Carta de Condução nr: ${driver.licenseId}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Data de Nascimento: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(driver.dateOfBirth))}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AppointmentsList(
    appointments: List<Appointment>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = appointments,
            key = { it.id }
        ) { appointment ->
            AppointmentCard(appointment = appointment)
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appointment Type Icon
            Icon(
                imageVector = when (appointment.type) {
                    AppointmentType.RENOVACAO -> Icons.Default.EventRepeat
                    AppointmentType.SEGUNDA_VIA -> Icons.Default.CalendarMonth
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            // Appointment Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (appointment.type) {
                        AppointmentType.RENOVACAO -> "Renovação"
                        AppointmentType.SEGUNDA_VIA -> "Segunda Via"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Data: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(appointment.date))}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Horário: ${appointment.time}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                StatusChip(status = appointment.status)
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: AppointmentStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = when (status) {
            AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primaryContainer
            AppointmentStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            AppointmentStatus.CANCELLED -> Color(0xFFE57373).copy(alpha = 0.2f)
        }
    ) {
        Text(
            text = when (status) {
                AppointmentStatus.SCHEDULED -> "Agendado"
                AppointmentStatus.COMPLETED -> "Concluído"
                AppointmentStatus.CANCELLED -> "Cancelado"
            },
            color = when (status) {
                AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
                AppointmentStatus.COMPLETED -> Color(0xFF4CAF50)
                AppointmentStatus.CANCELLED -> Color(0xFFE57373)
            },
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyAppointmentsView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhuma consulta agendada",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "As suas consultas aparecerão aqui",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}