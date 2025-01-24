package com.acsunmz.datacapture.feature.biometrics.camerax.idscan

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.acsunmz.datacapture.R

// ChooserScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooserScreen(
    onDocumentTypeSelected: (DocumentType) -> Unit,
    onBackPress: () -> Unit,
    navController: NavHostController
) {
    val documentTypes = listOf(
        DocumentType.MozambicanID,
        DocumentType.Passport,
        DocumentType.ElectionCard
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Escolha o Documento") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.id_card_default),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "1. Digitalize o seu documento de identificação",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 24.dp, bottom = 40.dp)
            )

//            Text(
//                text = "Tipo de Documento",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.padding(bottom = 10.dp)
//            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxHeight()
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

//@Preview
//@Composable
//fun ChooserScreenPreview(){
//    val documentTypes = listOf(
//        DocumentType.MozambicanID,
//        DocumentType.Passport,
//        DocumentType.ElectionCard
//    )
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .fillMaxWidth()
//    ) {
//
//        Icon(
//            painter = painterResource(
//                R.drawable.id_card_default
//            ),
//            contentDescription = null,
//            modifier = Modifier
//                .size(100.dp)
//                .align(Alignment.CenterHorizontally)
//            ,
//            tint = MaterialTheme.colorScheme.primary
//        )
//
//        Text(
//            text = "Scan your identification document",
//            style = MaterialTheme.typography.titleLarge,
//            modifier = Modifier.padding(top = 24.dp, bottom = 40.dp)
//        )
//
//
////        Text(
////            text = "Document type",
////            style = MaterialTheme.typography.titleMedium,
////            modifier = Modifier
////                .padding(top = 10.dp, bottom = 10.dp)
////
////        )
//
//        LazyColumn(
//            verticalArrangement = Arrangement.spacedBy(40.dp)
//        ) {
//            items(documentTypes) { documentType ->
//                DocumentTypeCard(
//                    documentType = documentType,
//                    onClick = {  }
//                )
//            }
//        }
//    }
//}
