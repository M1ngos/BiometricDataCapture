package com.acsunmz.datacapture.feature.biometrics.camerax.idscan

import android.graphics.drawable.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.acsunmz.datacapture.R

// ChooserScreen.kt
@Composable
fun ChooserScreen(
    onDocumentTypeSelected: (DocumentType) -> Unit
) {
    val documentTypes = listOf(
        DocumentType.MozambicanID,
        DocumentType.Passport,
        DocumentType.ElectionCard
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(
                R.drawable.id_card_default
            ),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
            ,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Scan your identification document",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 24.dp, bottom = 40.dp)
        )


        Text(
            text = "Document type",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(top = 10.dp, bottom = 10.dp)

        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            items(documentTypes) { documentType ->
                DocumentTypeCard(
                    documentType = documentType,
                    onClick = { onDocumentTypeSelected(documentType) }
                )
            }
        }
    }
}

@Preview
@Composable
fun ChooserScreenPreview(){
    val documentTypes = listOf(
        DocumentType.MozambicanID,
        DocumentType.Passport,
        DocumentType.ElectionCard
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .fillMaxWidth()
    ) {

        Icon(
            painter = painterResource(
                R.drawable.id_card_default
            ),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
            ,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Scan your identification document",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 24.dp, bottom = 40.dp)
        )


        Text(
            text = "Document type",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(top = 10.dp, bottom = 10.dp)

        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            items(documentTypes) { documentType ->
                DocumentTypeCard(
                    documentType = documentType,
                    onClick = {  }
                )
            }
        }
    }
}

@Composable
private fun DocumentTypeCard(
    documentType: DocumentType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = documentType.icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = documentType.title,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}