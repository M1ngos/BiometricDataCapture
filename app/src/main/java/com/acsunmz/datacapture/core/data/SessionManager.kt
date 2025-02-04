package com.acsunmz.datacapture.core.data

import android.content.Context
import android.content.SharedPreferences
import com.acsunmz.datacapture.core.model.Driver
import com.acsunmz.datacapture.ui.idscan.IdCardData
import com.google.gson.Gson

object SessionManager {

    private const val PREF_NAME = "app_session"
    private const val KEY_DRIVER = "driver"
    private const val KEY_ID_CARD_DATA = "id_card_data"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    // Save driver data
    fun saveDriver(driver: Driver) {
        val driverJson = Gson().toJson(driver)
        editor.putString(KEY_DRIVER, driverJson).apply()
    }

    // Get driver data
    fun getDriver(): Driver? {
        val driverJson = sharedPreferences.getString(KEY_DRIVER, null)
        return if (driverJson != null) {
            Gson().fromJson(driverJson, Driver::class.java)
        } else {
            null
        }
    }

    // Save ID Card Data
    fun saveIdCardData(idCardData: IdCardData) {
        val idCardJson = Gson().toJson(idCardData)
        editor.putString(KEY_ID_CARD_DATA, idCardJson).apply()
    }

    // Get ID Card Data
    fun getIdCardData(): IdCardData? {
        val idCardJson = sharedPreferences.getString(KEY_ID_CARD_DATA, null)
        return if (idCardJson != null) {
            Gson().fromJson(idCardJson, IdCardData::class.java)
        } else {
            null
        }
    }

    // Clear session
    fun clearSession() {
        editor.remove(KEY_DRIVER).apply()
        editor.remove(KEY_ID_CARD_DATA).apply()
    }
}


/*
object SessionManager {

    private const val PREFS_NAME = "app_session"
    private const val KEY_DRIVER_ID = "driver_id"
    private const val KEY_LICENSE_ID = "license_id"
    private const val KEY_DRIVER_NAME = "driver_name"
    private const val KEY_DOB = "date_of_birth"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveDriver(driver: Driver) {
        with(sharedPreferences.edit()) {
            putInt(KEY_DRIVER_ID, driver.id)
            putString(KEY_LICENSE_ID, driver.licenseId)
            putString(KEY_DRIVER_NAME, driver.name)
            putLong(KEY_DOB, driver.dateOfBirth)
            apply()
        }
    }

    fun getDriver(): Driver {
        val id = sharedPreferences.getInt(KEY_DRIVER_ID, -1)
//        if (id == -1) return null // No driver info saved

        val licenseId = sharedPreferences.getString(KEY_LICENSE_ID, "") ?: ""
        val name = sharedPreferences.getString(KEY_DRIVER_NAME, "") ?: ""
        val dob = sharedPreferences.getLong(KEY_DOB, 0)

        return Driver(id, licenseId, name, dob)
    }

    fun clearSession() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}
*/