package com.jobis.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ActivityTest {

    @Test
    fun `Activity 테이블 이름이 올바르게 설정되어야 함`() {
        assertEquals("activity", Activity.tableName)
    }

    @Test
    fun `Activity 테이블의 모든 컬럼이 정의되어야 함`() {
        assertNotNull(Activity.id)
        assertNotNull(Activity.startedAt)
        assertNotNull(Activity.endedAt)
        assertNotNull(Activity.description)
        assertNotNull(Activity.createdAt)
        assertNotNull(Activity.updatedAt)
    }

    @Test
    fun `nullable 컬럼들이 올바르게 설정되어야 함`() {
        assertTrue(Activity.endedAt.columnType.nullable)
        assertTrue(Activity.description.columnType.nullable)
    }

    @Test
    fun `NOT NULL 컬럼들이 올바르게 설정되어야 함`() {
        assertFalse(Activity.startedAt.columnType.nullable)
    }
}