package com.jobis.repository

import com.jobis.domain.JobApply
import com.jobis.domain.JobApplyStatus
import com.jobis.extension.TestDatabaseExtension
import com.jobis.extension.withTestTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(TestDatabaseExtension::class)
class JobApplyRepositoryTest {
    
    private val repository = JobApplyRepositoryImpl()
    
    @Test
    fun `createJobApply는 새로운 JobApply를 생성한다`() = withTestTransaction(JobApply) {
        val companyName = "카카오"
        val position = "백엔드 개발자"
        val jobPostingUrl = "https://careers.kakao.com/jobs/1"
        val appliedAt = LocalDateTime.of(2024, 1, 1, 14, 0)
        val nextEventDate = LocalDateTime.of(2024, 1, 10, 10, 0)
        val notes = "관심 있는 회사"
        
        val jobApply = repository.createJobApply(
            companyName = companyName,
            position = position,
            jobPostingUrl = jobPostingUrl,
            appliedAt = appliedAt,
            nextEventDate = nextEventDate,
            notes = notes
        )
        
        assertEquals(companyName, jobApply.companyName)
        assertEquals(position, jobApply.position)
        assertEquals(jobPostingUrl, jobApply.jobPostingUrl)
        assertEquals(appliedAt, jobApply.appliedAt)
        assertEquals(JobApplyStatus.APPLIED, jobApply.status)
        assertEquals(nextEventDate, jobApply.nextEventDate)
        assertEquals(notes, jobApply.notes)
        assertNull(jobApply.deletedAt)
        assertNotNull(jobApply.createdAt)
        assertNotNull(jobApply.updatedAt)
    }
    
    @Test
    fun `createJobApply는 nullable 필드들이 null이어도 JobApply를 생성한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply(
            companyName = "네이버",
            position = null,
            jobPostingUrl = null,
            appliedAt = null,
            nextEventDate = null,
            notes = null
        )
        
        assertEquals("네이버", jobApply.companyName)
        assertNull(jobApply.position)
        assertNull(jobApply.jobPostingUrl)
        assertNull(jobApply.appliedAt)
        assertEquals(JobApplyStatus.APPLIED, jobApply.status)
        assertNull(jobApply.nextEventDate)
        assertNull(jobApply.notes)
        assertNull(jobApply.deletedAt)
    }
    
    @Test
    fun `findAll은 삭제되지 않은 모든 JobApply를 createdAt 역순으로 반환한다`() = withTestTransaction(JobApply) {
        val jobApply1 = repository.createJobApply("회사1", "직무1", null, null, null, null)
        Thread.sleep(10)
        val jobApply2 = repository.createJobApply("회사2", "직무2", null, null, null, null)
        Thread.sleep(10)
        val jobApply3 = repository.createJobApply("회사3", "직무3", null, null, null, null)
        
        repository.deleteJobApply(jobApply2.id.value)
        
        val result = repository.findAll()
        
        assertEquals(2, result.size)
        assertEquals("회사3", result[0].companyName)
        assertEquals("회사1", result[1].companyName)
    }
    
    @Test
    fun `findActiveJobApplies는 진행중인 JobApply만 반환한다`() = withTestTransaction(JobApply) {
        val jobApply1 = repository.createJobApply("회사1", "직무1", null, null, null, null)
        val jobApply2 = repository.createJobApply("회사2", "직무2", null, null, null, null)
        val jobApply3 = repository.createJobApply("회사3", "직무3", null, null, null, null)
        
        repository.updateStatus(jobApply1.id.value, JobApplyStatus.PASSED)
        repository.updateStatus(jobApply2.id.value, JobApplyStatus.REJECTED)
        
        val result = repository.findActiveJobApplies()
        
        assertEquals(1, result.size)
        assertEquals("회사3", result[0].companyName)
        assertEquals(JobApplyStatus.APPLIED, result[0].status)
    }
    
    @Test
    fun `findById는 ID로 JobApply를 조회한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("테스트회사", "테스트직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        
        val found = repository.findById(jobApplyId)
        
        assertNotNull(found)
        assertEquals(jobApplyId, found!!.id.value)
        assertEquals("테스트회사", found.companyName)
        assertEquals("테스트직무", found.position)
    }
    
    @Test
    fun `findById는 삭제된 JobApply에 대해 null을 반환한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("삭제될회사", "삭제될직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        
        repository.deleteJobApply(jobApplyId)
        val found = repository.findById(jobApplyId)
        
        assertNull(found)
    }
    
    @Test
    fun `findByStatus는 특정 상태의 JobApply를 반환한다`() = withTestTransaction(JobApply) {
        val jobApply1 = repository.createJobApply("회사1", "직무1", null, null, null, null)
        val jobApply2 = repository.createJobApply("회사2", "직무2", null, null, null, null)
        val jobApply3 = repository.createJobApply("회사3", "직무3", null, null, null, null)
        
        repository.updateStatus(jobApply1.id.value, JobApplyStatus.INTERVIEW_SCHEDULED)
        repository.updateStatus(jobApply2.id.value, JobApplyStatus.INTERVIEW_SCHEDULED)
        
        val result = repository.findByStatus(JobApplyStatus.INTERVIEW_SCHEDULED)
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == JobApplyStatus.INTERVIEW_SCHEDULED })
    }
    
    @Test
    fun `findByCompanyName은 회사명으로 JobApply를 조회한다`() = withTestTransaction(JobApply) {
        repository.createJobApply("카카오", "백엔드", null, null, null, null)
        repository.createJobApply("카카오", "프론트엔드", null, null, null, null)
        repository.createJobApply("네이버", "백엔드", null, null, null, null)
        
        val result = repository.findByCompanyName("카카오")
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.companyName == "카카오" })
    }
    
    @Test
    fun `findByAppliedDateRange는 지원일 범위로 JobApply를 조회한다`() = withTestTransaction(JobApply) {
        val jobApply1 = repository.createJobApply("회사1", "직무1", null, 
            LocalDateTime.of(2024, 1, 1, 10, 0), null, null)
        val jobApply2 = repository.createJobApply("회사2", "직무2", null, 
            LocalDateTime.of(2024, 1, 2, 10, 0), null, null)
        val jobApply3 = repository.createJobApply("회사3", "직무3", null, 
            LocalDateTime.of(2024, 1, 3, 10, 0), null, null)
        
        val result = repository.findByAppliedDateRange(
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 2)
        )
        
        assertEquals(2, result.size)
        assertEquals("회사2", result[0].companyName)
        assertEquals("회사1", result[1].companyName)
    }
    
    @Test
    fun `updateStatus는 상태를 수정한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("테스트회사", "테스트직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        
        Thread.sleep(10)
        val updated = repository.updateStatus(jobApplyId, JobApplyStatus.INTERVIEW_SCHEDULED)
        
        assertTrue(updated)
        val reloadedJobApply = repository.findById(jobApplyId)
        assertNotNull(reloadedJobApply)
        assertEquals(JobApplyStatus.INTERVIEW_SCHEDULED, reloadedJobApply!!.status)
    }
    
    @Test
    fun `updateAppliedAt은 지원일을 수정한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("테스트회사", "테스트직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        val newAppliedAt = LocalDateTime.of(2024, 1, 1, 14, 0)
        
        Thread.sleep(10)
        val updated = repository.updateAppliedAt(jobApplyId, newAppliedAt)
        
        assertTrue(updated)
        val reloadedJobApply = repository.findById(jobApplyId)
        assertNotNull(reloadedJobApply)
        assertEquals(newAppliedAt, reloadedJobApply!!.appliedAt)
    }
    
    @Test
    fun `updateNextEventDate는 다음 일정을 수정한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("테스트회사", "테스트직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        val newNextEventDate = LocalDateTime.of(2024, 1, 10, 10, 0)
        
        Thread.sleep(10)
        val updated = repository.updateNextEventDate(jobApplyId, newNextEventDate)
        
        assertTrue(updated)
        val reloadedJobApply = repository.findById(jobApplyId)
        assertNotNull(reloadedJobApply)
        assertEquals(newNextEventDate, reloadedJobApply!!.nextEventDate)
    }
    
    @Test
    fun `updateNotes는 메모를 수정한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("테스트회사", "테스트직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        val newNotes = "수정된 메모"
        
        Thread.sleep(10)
        val updated = repository.updateNotes(jobApplyId, newNotes)
        
        assertTrue(updated)
        val reloadedJobApply = repository.findById(jobApplyId)
        assertNotNull(reloadedJobApply)
        assertEquals(newNotes, reloadedJobApply!!.notes)
    }
    
    @Test
    fun `updateCompanyName은 회사명을 수정한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("원래회사", "테스트직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        val newCompanyName = "수정된회사"
        
        Thread.sleep(10)
        val updated = repository.updateCompanyName(jobApplyId, newCompanyName)
        
        assertTrue(updated)
        val reloadedJobApply = repository.findById(jobApplyId)
        assertNotNull(reloadedJobApply)
        assertEquals(newCompanyName, reloadedJobApply!!.companyName)
    }
    
    @Test
    fun `updatePosition은 직무를 수정한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("테스트회사", "원래직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        val newPosition = "수정된직무"
        
        Thread.sleep(10)
        val updated = repository.updatePosition(jobApplyId, newPosition)
        
        assertTrue(updated)
        val reloadedJobApply = repository.findById(jobApplyId)
        assertNotNull(reloadedJobApply)
        assertEquals(newPosition, reloadedJobApply!!.position)
    }
    
    @Test
    fun `updatePosition은 직무를 null로 수정할 수 있다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("테스트회사", "원래직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        
        Thread.sleep(10)
        val updated = repository.updatePosition(jobApplyId, null)
        
        assertTrue(updated)
        val reloadedJobApply = repository.findById(jobApplyId)
        assertNotNull(reloadedJobApply)
        assertNull(reloadedJobApply!!.position)
    }
    
    @Test
    fun `updateJobPostingUrl은 공고 URL을 수정한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("테스트회사", "테스트직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        val newJobPostingUrl = "https://example.com/jobs/1"
        
        Thread.sleep(10)
        val updated = repository.updateJobPostingUrl(jobApplyId, newJobPostingUrl)
        
        assertTrue(updated)
        val reloadedJobApply = repository.findById(jobApplyId)
        assertNotNull(reloadedJobApply)
        assertEquals(newJobPostingUrl, reloadedJobApply!!.jobPostingUrl)
    }
    
    @Test
    fun `update 메서드들은 삭제된 JobApply에 대해 false를 반환한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("삭제될회사", "삭제될직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        
        repository.deleteJobApply(jobApplyId)
        
        assertFalse(repository.updateStatus(jobApplyId, JobApplyStatus.INTERVIEW_SCHEDULED))
        assertFalse(repository.updateAppliedAt(jobApplyId, LocalDateTime.of(2024, 1, 1, 14, 0)))
        assertFalse(repository.updateNextEventDate(jobApplyId, LocalDateTime.of(2024, 1, 10, 10, 0)))
        assertFalse(repository.updateNotes(jobApplyId, "새 메모"))
        assertFalse(repository.updateCompanyName(jobApplyId, "새 회사"))
        assertFalse(repository.updatePosition(jobApplyId, "새 직무"))
        assertFalse(repository.updateJobPostingUrl(jobApplyId, "https://example.com"))
    }
    
    @Test
    fun `deleteJobApply는 JobApply를 soft delete한다`() = withTestTransaction(JobApply) {
        val jobApply = repository.createJobApply("삭제될회사", "삭제될직무", null, null, null, null)
        val jobApplyId = jobApply.id.value
        
        val deleted = repository.deleteJobApply(jobApplyId)
        
        assertTrue(deleted)
        assertNotNull(jobApply.deletedAt)
        
        val allJobApplies = repository.findAll()
        assertTrue(allJobApplies.none { it.id.value == jobApplyId })
    }
    
    @Test
    fun `deleteJobApplies는 여러 JobApply를 soft delete한다`() = withTestTransaction(JobApply) {
        val jobApply1 = repository.createJobApply("회사1", "직무1", null, null, null, null)
        val jobApply2 = repository.createJobApply("회사2", "직무2", null, null, null, null)
        val jobApply3 = repository.createJobApply("회사3", "직무3", null, null, null, null)
        
        val deletedCount = repository.deleteJobApplies(listOf(
            jobApply1.id.value, 
            jobApply3.id.value
        ))
        
        assertEquals(2, deletedCount)
        
        val remainingJobApplies = repository.findAll()
        assertEquals(1, remainingJobApplies.size)
        assertEquals("회사2", remainingJobApplies[0].companyName)
    }
    
    @Test
    fun `findByAppliedDateRange는 삭제된 JobApply를 제외한다`() = withTestTransaction(JobApply) {
        val jobApply1 = repository.createJobApply("회사1", "직무1", null, 
            LocalDateTime.of(2024, 1, 1, 10, 0), null, null)
        val jobApply2 = repository.createJobApply("삭제될회사", "삭제될직무", null, 
            LocalDateTime.of(2024, 1, 1, 12, 0), null, null)
        
        repository.deleteJobApply(jobApply2.id.value)
        
        val result = repository.findByAppliedDateRange(
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 1)
        )
        
        assertEquals(1, result.size)
        assertEquals("회사1", result[0].companyName)
    }
}