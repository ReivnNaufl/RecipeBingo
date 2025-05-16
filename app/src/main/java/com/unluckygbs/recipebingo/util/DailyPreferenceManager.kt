package com.unluckygbs.recipebingo.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dailyDataStore by preferencesDataStore(name = "daily_pref")

object DailyPreferenceManager {
    private val LAST_FETCH_DATE = stringPreferencesKey("last_fetch_date")

    suspend fun saveLastFetchDate(context: Context, date: String) {
        context.dailyDataStore.edit { preferences ->
            preferences[LAST_FETCH_DATE] = date
        }
    }

    suspend fun getLastFetchDate(context: Context): String? {
        return context.dailyDataStore.data
            .map { preferences -> preferences[LAST_FETCH_DATE] }
            .first()
    }
}