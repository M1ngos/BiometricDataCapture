package com.acsunmz.datacapture.feature.biometrics.camerax.idscan

import com.acsunmz.datacapture.R

data class DocumentField(
    val name: String,
    val regex: String? = null,
    val required: Boolean = true
)

sealed class DocumentType(
    val title: String,
    val icon: Int,
    val fields: List<DocumentField>
) {
    object IdCard : DocumentType(
        "ID Card",
        R.drawable.ic_id_card,
        listOf(
            DocumentField("Full Name", "[A-Za-z\\s]{2,50}"),
            DocumentField("ID Number", "[A-Z0-9]{8,12}"),
            DocumentField("Date of Birth", "(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])"),
            DocumentField("Address"),
            DocumentField("Photo Location", required = false)
        )
    )

    object Passport : DocumentType(
        "Passport",
        R.drawable.ic_passport,
        listOf(
            DocumentField("Full Name", "[A-Za-z\\s]{2,50}"),
            DocumentField("Passport Number", "[A-Z0-9]{6,9}"),
            DocumentField("Nationality", "[A-Za-z\\s]{2,50}"),
            DocumentField("Date of Birth", "(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])"),
            DocumentField("Expiry Date", "(20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])"),
            DocumentField("Photo Location", required = false)
        )
    )

    object ElectionCard : DocumentType(
        "Election Card",
        R.drawable.ic_election_card,
        listOf(
            DocumentField("Full Name", "[A-Za-z\\s]{2,50}"),
            DocumentField("Sex", "Male|Female|Other"),
            DocumentField("Date of Birth", "(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])"),
            DocumentField("Emitted", "(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])"),
            DocumentField("Residence"),
            DocumentField("ID Number", "[A-Z]{3}[0-9]{7}")
        )
    )
}