package com.jobis.config

import com.jobis.domain.Activity
import com.jobis.extension.TestDatabaseExtension
import com.jobis.extension.withTestTransaction
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestDatabaseExtension::class)
class DatabaseTest {

    @Test
    fun `데이터베이스 연결과 테이블 생성이 성공해야 함`() = withTestTransaction(Activity) {
        val result = Activity.selectAll()
        assertTrue(result.empty())
    }
}