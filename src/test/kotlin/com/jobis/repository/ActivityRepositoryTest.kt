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
        val description = "공부"
        
        val activity = repository.createActivity(description)
        
        assertEquals(description, activity.description)
        assertNull(activity.endedAt)
        assertNull(activity.deletedAt)
        assertNotNull(activity.startedAt)
        assertNotNull(activity.createdAt)
        assertNotNull(activity.updatedAt)
    }
    
    @Test
    fun `createActivity는 description이 null이어도 Activity를 생성한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity(null)
        
        assertNull(activity.description)
        assertNull(activity.endedAt)
        assertNull(activity.deletedAt)
        assertNotNull(activity.startedAt)
        assertNotNull(activity.createdAt)
        assertNotNull(activity.updatedAt)
    }
    
    @Test
    fun `findAll은 삭제되지 않은 모든 Activity를 startedAt 순으로 반환한다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity("첫번째 활동")
        Thread.sleep(10)
        val activity2 = repository.createActivity("두번째 활동")
        Thread.sleep(10)
        val activity3 = repository.createActivity("세번째 활동")
        
        // 하나는 삭제
        repository.deleteActivity(activity2.id.value)
        
        val result = repository.findAll()
        
        assertEquals(2, result.size)
        assertEquals("첫번째 활동", result[0].description)
        assertEquals("세번째 활동", result[1].description)
    }
    
    @Test
    fun `findById는 ID로 Activity를 조회한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity("테스트 활동")
        val activityId = activity.id.value
        
        val found = repository.findById(activityId)
        
        assertNotNull(found)
        assertEquals(activityId, found!!.id.value)
        assertEquals("테스트 활동", found.description)
    }
    
    @Test
    fun `findById는 삭제된 Activity에 대해 null을 반환한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity("삭제될 활동")
        val activityId = activity.id.value
        
        repository.deleteActivity(activityId)
        val found = repository.findById(activityId)
        
        assertNull(found)
    }
    
    @Test
    fun `updateStartedAt은 시작 시간을 수정한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity("테스트 활동")
        val activityId = activity.id.value
        val newStartedAt = LocalDateTime.of(2024, 1, 1, 11, 0)
        
        Thread.sleep(10)
        val updated = repository.updateStartedAt(activityId, newStartedAt)
        
        assertTrue(updated)
        val reloadedActivity = repository.findById(activityId)
        assertNotNull(reloadedActivity)
        assertEquals(newStartedAt, reloadedActivity!!.startedAt)
    }
    
    @Test
    fun `updateEndedAt은 종료 시간을 수정한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity("테스트 활동")
        val activityId = activity.id.value
        val newEndedAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        
        Thread.sleep(10)
        val updated = repository.updateEndedAt(activityId, newEndedAt)
        
        assertTrue(updated)
        val reloadedActivity = repository.findById(activityId)
        assertNotNull(reloadedActivity)
        assertEquals(newEndedAt, reloadedActivity!!.endedAt)
    }
    
    @Test
    fun `updateDescription은 설명을 수정한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity("원래 설명")
        val activityId = activity.id.value
        val newDescription = "수정된 설명"
        
        Thread.sleep(10)
        val updated = repository.updateDescription(activityId, newDescription)
        
        assertTrue(updated)
        val reloadedActivity = repository.findById(activityId)
        assertNotNull(reloadedActivity)
        assertEquals(newDescription, reloadedActivity!!.description)
    }
    
    @Test
    fun `update 메서드들은 삭제된 Activity에 대해 false를 반환한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity("삭제될 활동")
        val activityId = activity.id.value
        
        repository.deleteActivity(activityId)
        
        assertFalse(repository.updateStartedAt(activityId, LocalDateTime.of(2024, 1, 1, 11, 0)))
        assertFalse(repository.updateEndedAt(activityId, LocalDateTime.of(2024, 1, 1, 12, 0)))
        assertFalse(repository.updateDescription(activityId, "새 설명"))
    }
    
    @Test
    fun `deleteActivity는 Activity를 soft delete한다`() = withTestTransaction(Activity) {
        val activity = repository.createActivity("삭제될 활동")
        val activityId = activity.id.value
        
        val deleted = repository.deleteActivity(activityId)
        
        assertTrue(deleted)
        assertNotNull(activity.deletedAt)
        
        val allActivities = repository.findAll()
        assertTrue(allActivities.none { it.id.value == activityId })
    }
    
    @Test
    fun `deleteActivities는 여러 Activity를 soft delete한다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity("활동1")
        val activity2 = repository.createActivity("활동2")
        val activity3 = repository.createActivity("활동3")
        
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
        val activity1 = repository.createActivity("활동1")
        activity1.endedAt = LocalDateTime.of(2024, 1, 1, 11, 0)
        activity1.startedAt = LocalDateTime.of(2024, 1, 1, 10, 0)
        
        val activity2 = repository.createActivity("활동2")
        activity2.endedAt = LocalDateTime.of(2024, 1, 1, 15, 30)
        activity2.startedAt = LocalDateTime.of(2024, 1, 1, 14, 0)
        
        val activity3 = repository.createActivity("진행중인 활동")
        activity3.startedAt = LocalDateTime.of(2024, 1, 1, 16, 0)
        
        val totalDuration = repository.getTotalDurationByDate(LocalDate.of(2024, 1, 1))
        
        assertEquals(Duration.ofMinutes(150), totalDuration)
    }
    
    @Test
    fun `getTotalDurationByDate는 해당 날짜에 완료된 Activity가 없으면 ZERO를 반환한다`() = withTestTransaction(Activity) {
        val date = LocalDate.of(2024, 1, 1)
        
        val totalDuration = repository.getTotalDurationByDate(date)
        
        assertEquals(Duration.ZERO, totalDuration)
    }
    
    @Test
    fun `getTotalDurationByDateRange는 날짜 범위의 총 소요시간을 반환한다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity("1일 활동")
        activity1.endedAt = LocalDateTime.of(2024, 1, 1, 11, 0)
        activity1.startedAt = LocalDateTime.of(2024, 1, 1, 10, 0)
        
        val activity2 = repository.createActivity("2일 활동")
        activity2.endedAt = LocalDateTime.of(2024, 1, 2, 12, 0)
        activity2.startedAt = LocalDateTime.of(2024, 1, 2, 10, 0)
        
        val activity3 = repository.createActivity("3일 활동")
        activity3.endedAt = LocalDateTime.of(2024, 1, 3, 11, 30)
        activity3.startedAt = LocalDateTime.of(2024, 1, 3, 10, 0)
        
        val totalDuration = repository.getTotalDurationByDateRange(
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 2)
        )
        
        assertEquals(Duration.ofHours(3), totalDuration)
    }
    
    @Test
    fun `삭제된 Activity는 duration 계산에서 제외된다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity("활동1")
        activity1.endedAt = LocalDateTime.of(2024, 1, 1, 11, 0)
        activity1.startedAt = LocalDateTime.of(2024, 1, 1, 10, 0)
        
        val activity2 = repository.createActivity("삭제될 활동")
        activity2.endedAt = LocalDateTime.of(2024, 1, 1, 16, 0)
        activity2.startedAt = LocalDateTime.of(2024, 1, 1, 14, 0)
        
        repository.deleteActivity(activity2.id.value)
        
        val totalDuration = repository.getTotalDurationByDate(LocalDate.of(2024, 1, 1))
        
        assertEquals(Duration.ofHours(1), totalDuration)
    }
    
    @Test
    fun `findByDateRange는 지정된 날짜 범위의 Activity를 반환한다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity("1일 활동")
        activity1.startedAt = LocalDateTime.of(2024, 1, 1, 10, 0)
        
        val activity2 = repository.createActivity("2일 활동")
        activity2.startedAt = LocalDateTime.of(2024, 1, 2, 10, 0)
        
        val activity3 = repository.createActivity("3일 활동")
        activity3.startedAt = LocalDateTime.of(2024, 1, 3, 10, 0)
        
        val activities = repository.findByDateRange(
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 2)
        )
        
        assertEquals(2, activities.size)
        assertEquals("1일 활동", activities[0].description)
        assertEquals("2일 활동", activities[1].description)
    }
    
    @Test
    fun `findByDateRange는 삭제된 Activity를 제외한다`() = withTestTransaction(Activity) {
        val activity1 = repository.createActivity("활동1")
        activity1.startedAt = LocalDateTime.of(2024, 1, 1, 10, 0)
        
        val activity2 = repository.createActivity("삭제될 활동")
        activity2.startedAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        
        repository.deleteActivity(activity2.id.value)
        
        val activities = repository.findByDateRange(
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 1)
        )
        
        assertEquals(1, activities.size)
        assertEquals("활동1", activities[0].description)
    }
}