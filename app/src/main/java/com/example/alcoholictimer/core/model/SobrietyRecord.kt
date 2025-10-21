package com.example.alcoholictimer.core.model

import com.example.alcoholictimer.core.util.PercentUtils
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class SobrietyRecord(
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val targetDays: Int,
    val actualDays: Int,
    val isCompleted: Boolean,
    val status: String,
    val createdAt: Long,
    val percentage: Int? = null,
    val memo: String? = null
) {
    val achievedPercentage: Int
        get() = percentage ?: if (targetDays > 0) {
            PercentUtils.roundPercent((actualDays.toDouble() / targetDays.toDouble()) * 100.0)
        } else 0

    val startDateString: String get() = formatDate(startTime)
    val endDateString: String get() = formatDate(endTime)

    val achievedLevel: Int
        get() = when {
            actualDays < 7 -> 1
            actualDays < 30 -> 2
            actualDays < 90 -> 3
            actualDays < 365 -> 4
            else -> 5
        }

    val levelTitle: String
        get() = when {
            actualDays < 7 -> "시작"
            actualDays < 30 -> "작심 7일"
            actualDays < 90 -> "한 달 클리어"
            actualDays < 365 -> "3개월 클리어"
            else -> "절제의 레전드"
        }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getDefault()
        return sdf.format(Date(timestamp))
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("startTime", startTime)
        put("endTime", endTime)
        put("targetDays", targetDays)
        put("actualDays", actualDays)
        put("isCompleted", isCompleted)
        put("status", status)
        put("createdAt", createdAt)
        percentage?.let { put("percentage", it) }
        memo?.let { put("memo", it) }
    }

    companion object {
        fun fromJson(json: JSONObject): SobrietyRecord = SobrietyRecord(
            id = json.getString("id"),
            startTime = json.getLong("startTime"),
            endTime = json.getLong("endTime"),
            targetDays = json.getInt("targetDays"),
            actualDays = json.getInt("actualDays"),
            isCompleted = json.getBoolean("isCompleted"),
            status = json.getString("status"),
            createdAt = json.getLong("createdAt"),
            percentage = if (json.has("percentage")) json.getInt("percentage") else null,
            memo = if (json.has("memo")) json.getString("memo") else null
        )

        fun toJsonArray(records: List<SobrietyRecord>): String {
            val jsonArray = JSONArray()
            records.forEach { record -> jsonArray.put(record.toJson()) }
            return jsonArray.toString()
        }

        fun fromJsonArray(jsonString: String): List<SobrietyRecord> {
            val records = mutableListOf<SobrietyRecord>()
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    records.add(fromJson(jsonObject))
                }
            } catch (_: Exception) { }
            return records
        }
    }
}

