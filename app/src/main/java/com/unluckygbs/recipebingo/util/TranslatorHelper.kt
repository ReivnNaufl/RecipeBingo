package com.unluckygbs.recipebingo.util

import android.util.Log
import com.google.mlkit.nl.translate.*
import kotlinx.coroutines.tasks.await

class TranslatorHelper {
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH) // dari English
        .setTargetLanguage(TranslateLanguage.INDONESIAN) // ke Indonesian
        .build()

    private val translator = Translation.getClient(options)

    suspend fun translateText(text: String): String? {
        return try {
            // Unduh model jika belum tersedia
            translator.downloadModelIfNeeded().await()
            // Lakukan terjemahan
            translator.translate(text).await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun downloadModel() {
        try {
            translator.downloadModelIfNeeded().await()
            Log.d("TranslatorHelper", "Downloading model if needed")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}