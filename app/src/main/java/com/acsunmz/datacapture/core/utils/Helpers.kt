package com.acsunmz.datacapture.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.acsunmz.datacapture.core.model.DocumentInfo
import com.google.gson.Gson

import java.io.ByteArrayInputStream

fun convert(jsonUri: Uri, context: Context): DocumentInfo? {
    if (jsonUri != null) {
        val inputStream = context.contentResolver.openInputStream(jsonUri)
        val json = inputStream?.bufferedReader().use { it?.readText() }

        val document = Gson().fromJson(json, DocumentInfo::class.java)
        Log.d("DEBUG", "Documento recebido: $document")
        Log.e("Document",document.toString())
        return document
    }
    return null
}

fun loadImageFromUri(context: Context, uri: Uri?): Bitmap? {
    return uri?.let {
        try {
            Log.d("App2", "Tentando abrir URI: $uri")
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val bytes =
                    inputStream.readBytes()
                Log.d("App2", "Bytes lidos de $uri: ${bytes.size}")
                BitmapFactory.decodeStream(ByteArrayInputStream(bytes))
            } ?: run {
                Log.e("App2", "InputStream nulo para $uri")
                null
            }
        } catch (e: Exception) {
            Log.e("LoadImage", "Erro ao carregar $uri: ${e.message}", e)
            null
        }
    }
}