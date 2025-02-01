//package com.acsunmz.datacapture.ui.idscan
//
//@Composable
//fun ConfirmScan(
//    onProceed: () -> Unit,
//    viewModel: IdScanViewModel = viewModel()
//) {
//    val context = LocalContext.current
//
//    // Retrieve the saved ID card data from shared preferences
//    val idCardData = remember {
//        SessionManager.initialize(context) // Ensure it's initialized
//        SessionManager.getIdCardData()
//    }
//
//    Log.d("IdUpload", "value in confirmScan: $idCardData")
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp)
//        ) {
//            // Document Data Card
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
//            ) {
//                Column(
//                    modifier = Modifier.padding(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Text(
//                        text = "Dados do Documento",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold
//                    )
//                    idCardData?.let {
//                        DriverInfoItem("Nome", it.fullName)
//                        DriverInfoItem("Número da Carta", it.idNumber)
//                        DriverInfoItem("Data de Nascimento", it.dateOfBirth)
//                        DriverInfoItem("Altura", it.height)
//                        DriverInfoItem("Sexo", it.sex)
//                        DriverInfoItem("Local de Nascimento", it.birthPlace)
//                        DriverInfoItem("Endereço", it.address)
//                    } ?: Text("No document data available")
//                }
//            }
//
//            // ... other UI components ...
//
//        }
//    }
//}
//
//
//// After parsing the JSON and before navigation
//_idCardData.value = IdCardData(
//documentType = frontData.optString("document_type", "Unknown"),
//idNumber = frontData.optString("id_number", "N/A"),
//fullName = frontData.optString("full_name", "Unknown"),
//dateOfBirth = frontData.optString("date_of_birth", "N/A"),
//height = frontData.optString("height", "N/A"),
//sex = frontData.optString("sex", "N/A"),
//birthPlace = frontData.optString("birth_place", "N/A"),
//address = frontData.optString("address", "N/A")
//)
//
//Log.d("IdUpload", "value: ${_idCardData.value}")
//
//// Save it in shared preferences
//_idCardData.value?.let { SessionManager.saveIdCardData(it) }
//
//idUploadStatus = IdUploadStatus.Success("ID card processed successfully")
//viewModelScope.launch {
//    delay(1000)
//    shouldNavigate = true
//}
//
//
//object SessionManager {
//
//    private const val PREF_NAME = "app_session"
//    private const val KEY_DRIVER = "driver"
//    private const val KEY_ID_CARD_DATA = "id_card_data"
//
//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var editor: SharedPreferences.Editor
//
//    fun initialize(context: Context) {
//        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        editor = sharedPreferences.edit()
//    }
//
//    // Save driver data
//    fun saveDriver(driver: Driver) {
//        val driverJson = Gson().toJson(driver)
//        editor.putString(KEY_DRIVER, driverJson).apply()
//    }
//
//    // Get driver data
//    fun getDriver(): Driver? {
//        val driverJson = sharedPreferences.getString(KEY_DRIVER, null)
//        return if (driverJson != null) {
//            Gson().fromJson(driverJson, Driver::class.java)
//        } else {
//            null
//        }
//    }
//
//    // Save ID Card Data
//    fun saveIdCardData(idCardData: IdCardData) {
//        val idCardJson = Gson().toJson(idCardData)
//        editor.putString(KEY_ID_CARD_DATA, idCardJson).apply()
//    }
//
//    // Get ID Card Data
//    fun getIdCardData(): IdCardData? {
//        val idCardJson = sharedPreferences.getString(KEY_ID_CARD_DATA, null)
//        return if (idCardJson != null) {
//            Gson().fromJson(idCardJson, IdCardData::class.java)
//        } else {
//            null
//        }
//    }
//
//    // Clear session
//    fun clearSession() {
//        editor.remove(KEY_DRIVER).apply()
//        editor.remove(KEY_ID_CARD_DATA).apply()
//    }
//}
