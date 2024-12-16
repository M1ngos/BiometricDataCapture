package com.acsunmz.datacapture.feature.biometrics.camerax.idscan

import com.acsunmz.datacapture.R


sealed class CardSide {
    object Front : CardSide()
    object Back : CardSide()
}

data class DocumentField(
    val name: String,
    val regex: String? = null,
    val required: Boolean = true,
    val searchHint: String,
    val side: CardSide = CardSide.Front
)

sealed class DocumentType(
    val title: String,
    val icon: Int,
    val fields: List<DocumentField>
) {
    object MozambicanID : DocumentType(
        "Mozambican ID Card",
        R.drawable.ic_id_card,
        listOf(
            // Front side fields
//            DocumentField(
//                name = "Nº:",
//                regex = "\\d{13}[A-Z]",
//                required = true,
//                searchHint = "Look for 13 digits followed by a letter",
//                side = CardSide.Front
//            ),
            DocumentField(
                name = "Nome / Name",
                regex = "[A-ZÁÉÍÓÚÇÑ\\s]{2,50}",
                required = true,
                searchHint = "Look for text after Nome / Name:",
                side = CardSide.Front
            ),
            DocumentField(
                name = "Data de Nascimento / Date of Birth",
                regex = "\\d{2}/\\d{2}/\\d{4}",
                required = true,
                searchHint = "Look for date format DD/MM/YYYY",
                side = CardSide.Front
            ),
            DocumentField(
                name = "Naturalidade / Place of Birth",
                regex = "[A-ZÁÉÍÓÚÇÑ\\s-]{2,50}",
                required = true,
                searchHint = "Look for text after Naturalidade:",
                side = CardSide.Front
            ),
            DocumentField(
                name = "Altura / Height",
                regex = "\\d{1,}[.,]\\d{2}",
                required = false,
                searchHint = "Look for height in meters",
                side = CardSide.Front
            ),
            DocumentField(
                name = "Local de Residência / Address",
                regex = "[A-ZÁÉÍÓÚÇÑ\\s.,0-9-]{2,100}",
                required = true,
                searchHint = "Look for text after Address:",
                side = CardSide.Front
            ),
            // Back side fields
            DocumentField(
                name = "Data de Emissão / Issuance Date",
                regex = "\\d{2}/\\d{2}/\\d{4}",
                required = true,
                searchHint = "Look for issuance date in DD/MM/YYYY format",
                side = CardSide.Back
            ),
            DocumentField(
                name = "Válido Até / Expiry Date",
                regex = "\\d{2}/\\d{2}/\\d{4}",
                required = true,
                searchHint = "Look for expiry date in DD/MM/YYYY format",
                side = CardSide.Back
            ),
            DocumentField(
                name = "Emitido Em / Issued in",
                regex = "[A-ZÁÉÍÓÚÇÑ\\s-]{2,50}",
                required = true,
                searchHint = "Look for place of issuance",
                side = CardSide.Back
            ),
            DocumentField(
                name = "Estado Civil / Marital Status",
                regex = "SOLTEIRA?|CASADA?|DIVORCIADA?|VIÚVA?",
                required = true,
                searchHint = "Look for marital status",
                side = CardSide.Back
            ),
            DocumentField(
                name = "Nome do Pai / Father Name",
                regex = "[A-ZÁÉÍÓÚÇÑ\\s]{2,50}",
                required = true,
                searchHint = "Look for father's name",
                side = CardSide.Back
            ),
            DocumentField(
                name = "Nome da Mãe / Mother Name",
                regex = "[A-ZÁÉÍÓÚÇÑ\\s]{2,50}",
                required = true,
                searchHint = "Look for mother's name",
                side = CardSide.Back
            )
        )
    )

    object Passport : DocumentType(
        "Passport",
        R.drawable.ic_passport,
        listOf(
            DocumentField(
                "Full Name",
                "[A-Za-z\\s]{2,50}",
                searchHint = "Look for 13 digits followed by a letter"
            ),
            DocumentField(
                "Passport Number",
                "[A-Z0-9]{6,9}",
                searchHint = "Look for 13 digits followed by a letter"
            ),
            DocumentField(
                "Nationality",
                "[A-Za-z\\s]{2,50}",
                searchHint = "Look for 13 digits followed by a letter"
            ),
            DocumentField(
                "Date of Birth",
                "(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])",
                searchHint = "Look for 13 digits followed by a letter"
            ),
            DocumentField(
                "Expiry Date",
                "(20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])",
                searchHint = "Look for 13 digits followed by a letter"
            ),
            DocumentField(
                "Photo Location",
                required = false,
                searchHint = "Look for 13 digits followed by a letter"
            )
        )
    )

    object ElectionCard : DocumentType(
        "Election Card",
        R.drawable.ic_election_card,
        listOf(
            DocumentField(
                "Full Name",
                "[A-Za-z\\s]{2,50}",
                searchHint = "Look for 13 digits followed by a letter"
            ),
            DocumentField(
                "Sex",
                "Male|Female|Other",
                searchHint = "Look for 13 digits followed by a letter"
            ),
            DocumentField(
                "Date of Birth",
                "(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])",
                searchHint = "Look for 13 digits followed by a letter"
            ),
            DocumentField(
                "Emitted",
                "(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])",
                searchHint = "Look for 13 digits followed by a letter"
            ),
            DocumentField("Residence", searchHint = "Look for 13 digits followed by a letter"),
            DocumentField(
                "ID Number",
                "[A-Z]{3}[0-9]{7}",
                searchHint = "Look for 13 digits followed by a letter"
            )
        )
    )
}