package com.example.alcoholictimer.core.model

import org.junit.Assert.*
import org.junit.Test

class SobrietyRecordTest {

    @Test
    fun achievedPercentage_calculates_whenNull() {
        val now = System.currentTimeMillis()
        val r = SobrietyRecord(
            id = "r1",
            startTime = now - 5 * 24 * 60 * 60 * 1000L,
            endTime = now,
            targetDays = 10,
            actualDays = 5,
            isCompleted = false,
            status = "ACTIVE",
            createdAt = now - 5 * 24 * 60 * 60 * 1000L,
            percentage = null,
            memo = null
        )
        assertEquals(50, r.achievedPercentage)
    }

    @Test
    fun achievedPercentage_prefersExplicitPercentage() {
        val now = System.currentTimeMillis()
        val r = SobrietyRecord(
            id = "r2",
            startTime = now - 3 * 24 * 60 * 60 * 1000L,
            endTime = now,
            targetDays = 10,
            actualDays = 3,
            isCompleted = false,
            status = "ACTIVE",
            createdAt = now - 3 * 24 * 60 * 60 * 1000L,
            percentage = 42,
            memo = "custom"
        )
        assertEquals(42, r.achievedPercentage)
    }

    @Test
    fun achievedPercentage_targetZero_isZero() {
        val now = System.currentTimeMillis()
        val r = SobrietyRecord(
            id = "r3",
            startTime = now - 24 * 60 * 60 * 1000L,
            endTime = now,
            targetDays = 0,
            actualDays = 1,
            isCompleted = false,
            status = "ACTIVE",
            createdAt = now - 24 * 60 * 60 * 1000L,
            percentage = null,
            memo = null
        )
        assertEquals(0, r.achievedPercentage)
    }

    @Test
    fun level_and_title_mapping() {
        val now = System.currentTimeMillis()
        val make = { days: Int ->
            SobrietyRecord(
                id = "lvl$days",
                startTime = now - days * 24L * 60L * 60L * 1000L,
                endTime = now,
                targetDays = days + 1,
                actualDays = days,
                isCompleted = false,
                status = "ACTIVE",
                createdAt = now - days * 24L * 60L * 60L * 1000L,
                percentage = null,
                memo = null
            )
        }
        assertEquals(1, make(0).achievedLevel)
        assertEquals("시작", make(0).levelTitle)
        assertEquals(1, make(6).achievedLevel) // still <7
        assertEquals(2, make(7).achievedLevel) // <30
        assertEquals("작심 7일", make(10).levelTitle)
        assertEquals(3, make(30).achievedLevel) // 30 <90
        assertEquals("한 달 클리어", make(33).levelTitle)
        assertEquals(4, make(120).achievedLevel) // <365
        assertEquals(5, make(400).achievedLevel)
        assertEquals("절제의 레전드", make(400).levelTitle)
    }

    @Test
    fun json_roundTrip() {
        val now = System.currentTimeMillis()
        val r = SobrietyRecord(
            id = "json1",
            startTime = now - 2 * 24 * 60 * 60 * 1000L,
            endTime = now,
            targetDays = 5,
            actualDays = 2,
            isCompleted = false,
            status = "ACTIVE",
            createdAt = now - 2 * 24 * 60 * 60 * 1000L,
            percentage = null,
            memo = "메모"
        )
        val json = r.toJson()
        val r2 = SobrietyRecord.fromJson(json)
        assertEquals(r.id, r2.id)
        assertEquals(r.startTime, r2.startTime)
        assertEquals(r.endTime, r2.endTime)
        assertEquals(r.targetDays, r2.targetDays)
        assertEquals(r.actualDays, r2.actualDays)
        assertEquals(r.isCompleted, r2.isCompleted)
        assertEquals(r.status, r2.status)
        assertEquals(r.createdAt, r2.createdAt)
        assertEquals(r.memo, r2.memo)
    }
}

