package com.sweetapps.faceweight.core.data

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.sweetapps.faceweight.core.model.SobrietyRecord

object RecordsDataLoader {
    private const val TAG = "RecordsDataLoader"

    fun loadSobrietyRecords(context: Context): List<SobrietyRecord> = try {
        val sharedPref = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val recordsJson = sharedPref.getString("sobriety_records", "[]") ?: "[]"
        val records = SobrietyRecord.fromJsonArray(recordsJson)
        records.sortedByDescending { it.endTime }
    } catch (e: Exception) {
        Log.e(TAG, "기록 로딩 중 오류 발생", e)
        emptyList()
    }

    fun clearAllRecords(context: Context): Boolean = try {
        val sharedPref = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        sharedPref.edit { putString("sobriety_records", "[]") }
        true
    } catch (e: Exception) {
        Log.e(TAG, "모든 기록 삭제 중 오류", e)
        false
    }
}

