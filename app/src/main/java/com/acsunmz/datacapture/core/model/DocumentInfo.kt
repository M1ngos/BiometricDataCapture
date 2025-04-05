package com.acsunmz.datacapture.core.model

import android.graphics.Bitmap


data class Position(
    val x1: Int = 0,
    val y1: Int = 0,
    val x2: Int = 0,
    val y2: Int = 0
)


data class MRZ(
    val MRZ: String? = "",
    val DocumentClassCode: String? = "",
    val IssuingStateCode: String? = "",
    val DocumentNumber: String? = "",
    val IssuingStateName: String? = "",
    val Nationality: String? = "",
    val DateOfBirth: String? = "",
    val Sex: String? = "",
    val DateOfExpiry: String? = "",
    val NationalityCode: String? = "",
    val MRZType: String? = "",
    val Surname: String? = "",
    val GivenNames: String? = "",
    val PersonalNumber: String? = "",
    val FullName: String? = "",
    val Validation: Int? = 0,
    val IssuingDate: String? = "",
    var issued_in:String?=""
)

//@Parcelize
data class Images(
    val Document: String? = null,
    val Portrait: String? = null,
    val DocumentBack: String? = null
)



data class OtherDocument(
    var phone: String = "",
    var fullName: String = "",
    var docNr: String = "",
    var liveness: String = "",
    var docImage: String = ""
)



data class DocumentInfo(
    var id: Int = 0,
    var userFound:Boolean=false,
    var customerID:Int=0,
    var nuit: String="",
    var phone: String="",
    var photo : String = "",
    var fullName: String? = "",
    var address: String = "",
    var height: String = "",
    var sex: String = "",
    var dateOfBirth: String = "",
    var placeOfBirth: String = "",
    var personalNumber: String = "",
    var issuingStateCode: String = "",
    var issuingStateName: String = "",
    var documentName: String = "",
    var quality: Int = 0,
    var position: Position? = null,
    var images: Images? = null,
    var mrz : MRZ? = null,
    var marital_status:String?="",
    var mother_name:String?="",
    var father_name:String?="",
)




data class PhotosOnboarding(
    val liveness : Bitmap? = null,
    val frontFaceID : Bitmap? = null,
    val backFaceID : Bitmap? = null
)