package com.jobis.repository

import com.jobis.domain.Activity
import com.jobis.extension.TestDatabaseExtension
import com.jobis.extension.withTestTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(TestDatabaseExtension::class)
class ActivityRepositoryTest {
    
    private val repository = ActivityRepositoryImpl()
    
    @Test
    fun `createActivity는 새로운 Activity를 생성한다`() = withTestTransaction(Activity) {
        val startedAt = LocalDateTime.of(2024, 1, 1, 10, 0)
        val description = "공부"
        
        val activity = repository.createActivity(startedAt, description)
        
        assertEquals(startedAt, activity.startedAt)
        assertEquals(description, activity.description)
        assertNull(activity.endedAt)
        assertNull(activity.deletedAt)
        assertNotNull(activity.createdAt)
        assertNotNull(activity.updatedAt)
    }
    
    @Test
    fun `findByDateRange는 지정된 날짜 범위의 Activity를 반환한다`() = withTestTransaction(Activity) {
        val date1 = LocalDate.of(2024, 1, 1)
        val date2 = LocalDate.of(2024, 1, 2)
        
        repository.createActivity(LocalDateTime.of(2024, 1, 1, 10, 0), "1일 활동")
        repository.createActivity(LocalDateTime.of(2024, 1, 2, 10, 0), "2일 활동")
        repository.createActivity(LocalDateTime.of(2024, 1, 3, 10, 0), "3일 활동")
        
        val activities = repository.findByDateRange(date1, date2)
        
        assertEquals(2, activities.size)
        assertEquals("1일 활동", activities[0].description)
        assertEquals("2일 활동", activities[1].description)
        assertFalse(activities.any { it.description == "3일 활동" })
    }
    
    @Test
    fun `findAll은 삭제되지 않은 모든 Activity를 startedAt 순으로 반환한다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 10, 0), 
            "첫번째 활동"
        )
        val activity2 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 12, 0), 
            "두번째 활동"
        )
        val activity3 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 8, 0), 
            "세번째 활동 (가장 빠름)"
        )
        
        // 하나는 삭제
        repository.deleteActivity(activity2.id.value)
        
        val result = repository.findAll()
        
        assertEquals(2, result.size)
        assertEquals("세번째 활동 (가장 빠름)", result[0].description)
        assertEquals("첫번째 활동", result[1].description)
    }
    
    @Test
    fun `updateActivity는 Activity를 수정하고 updatedAt을 갱신한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 10, 0), 
            "원래 설명"
        )
        val activityId = activity.id.value
        val originalUpdatedAt = activity.updatedAt
        
        Thread.sleep(10)
        activity.description = "수정된 설명"
        repository.updateActivity(activity)
        
        val reloadedActivities = repository.findAll()
        val reloadedActivity = reloadedActivities.find { it.id.value == activityId }
        
        assertNotNull(reloadedActivity)
        assertEquals("수정된 설명", reloadedActivity!!.description)
        assertTrue(reloadedActivity.updatedAt.isAfter(originalUpdatedAt))
    }
    
    @Test
    fun `deleteActivity는 Activity를 soft delete한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 10, 0), 
            "삭제될 활동"
        )
        val activityId = activity.id.value
        
        val deleted = repository.deleteActivity(activityId)
        
        assertTrue(deleted)
        assertNotNull(activity.deletedAt)
        
        val allActivities = repository.findAll()
        assertTrue(allActivities.none { it.id.value == activityId })
    }
    
    @Test
    fun `deleteActivities는 여러 Activity를 soft delete한다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 10, 0), 
            "활동1"
        )
        val activity2 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 11, 0), 
            "활동2"
        )
        val activity3 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 12, 0), 
            "활동3"
        )
        
        val deletedCount = repository.deleteActivities(listOf(
            activity1.id.value, 
            activity3.id.value
        ))
        
        assertEquals(2, deletedCount)
        
        val remainingActivities = repository.findAll()
        assertEquals(1, remainingActivities.size)
        assertEquals("활동2", remainingActivities[0].description)
    }
    
    @Test
    fun `getTotalDurationByDate는 해당 날짜의 총 소요시간을 반환한다`() = withTestTransaction(Activity) {
        val date = LocalDate.of(2024, 1, 1)
        
        val activity1 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 10, 0), 
            "활동1"
        )
        activity1.endedAt = LocalDateTime.of(2024, 1, 1, 11, 0)        
        val activity2 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 14, 0), 
            "활동2"
        )
        activity2.endedAt = LocalDateTime.of(2024, 1, 1, 15, 30)        
        repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 16, 0), 
            "진행중인 활동"
        )
        
        val totalDuration = repository.getTotalDurationByDate(date)
        
        assertEquals(Duration.ofMinutes(150), totalDuration)    }
    
    @Test
    fun `getTotalDurationByDate는 해당 날짜에 완료된 Activity가 없으면 ZERO를 반환한다`() = withTestTransaction(Activity) {
        val date = LocalDate.of(2024, 1, 1)
        
        val totalDuration = repository.getTotalDurationByDate(date)
        
        assertEquals(Duration.ZERO, totalDuration)
    }
    
    @Test
    fun `getTotalDurationByDateRange는 날짜 범위의 총 소요시간을 반환한다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 10, 0), 
            "1일 활동"
        )
        activity1.endedAt = LocalDateTime.of(2024, 1, 1, 11, 0)        
        val activity2 = repository.createActivity(
            LocalDateTime.of(2024, 1, 2, 10, 0), 
            "2일 활동"
        )
        activity2.endedAt = LocalDateTime.of(2024, 1, 2, 12, 0)        
        val activity3 = repository.createActivity(
            LocalDateTime.of(2024, 1, 3, 10, 0), 
            "3일 활동"
        )
        activity3.endedAt = LocalDateTime.of(2024, 1, 3, 11, 30)        
        val totalDuration = repository.getTotalDurationByDateRange(
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 2)
        )
        
        assertEquals(Duration.ofHours(3), totalDuration)    }
    
    @Test
    fun `삭제된 Activity는 duration 계산에서 제외된다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 10, 0), 
            "활동1"
        )
        activity1.endedAt = LocalDateTime.of(2024, 1, 1, 11, 0)        
        val activity2 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 14, 0), 
            "삭제될 활동"
        )
        activity2.endedAt = LocalDateTime.of(2024, 1, 1, 16, 0)        
        repository.deleteActivity(activity2.id.value)
        
        val totalDuration = repository.getTotalDurationByDate(LocalDate.of(2024, 1, 1))
        
        assertEquals(Duration.ofHours(1), totalDuration)    }
    
    @Test
    fun `findByDateRange는 삭제된 Activity를 제외한다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 10, 0), 
            "활동1"
        )
        val activity2 = repository.createActivity(
            LocalDateTime.of(2024, 1, 1, 12, 0), 
            "삭제될 활동"
        )
        
        repository.deleteActivity(activity2.id.value)
        
        val activities = repository.findByDateRange(
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 1)
        )
        
        assertEquals(1, activities.size)
        assertEquals("활동1", activities[0].description)
    }
}