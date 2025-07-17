package com.jobis.repository

import com.jobis.domain.JobApply
import com.jobis.domain.JobPosting
import com.jobis.extension.TestDatabaseExtension
import com.jobis.extension.withTestTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

@ExtendWith(TestDatabaseExtension::class)
class JobPostingRepositoryTest {
    
    private val repository = JobPostingRepositoryImpl()
    
    @Test
    fun `createJobPosting은 새로운 JobPosting을 생성한다`() = withTestTransaction(JobPosting, JobApply) {
        val companyName = "카카오"
        val position = "백엔드 개발자"
        val jobPostingUrl = "https://careers.kakao.com/jobs/1"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val requirements = "자소서 3항목, 포트폴리오 필수"
        val notes = "관심 있는 회사"
        
        val jobPosting = repository.createJobPosting(
            companyName = companyName,
            position = position,
            jobPostingUrl = jobPostingUrl,
            startDate = startDate,
            endDate = endDate,
            requirements = requirements,
            notes = notes
        )
        
        assertEquals(companyName, jobPosting.companyName)
        assertEquals(position, jobPosting.position)
        assertEquals(jobPostingUrl, jobPosting.jobPostingUrl)
        assertEquals(startDate, jobPosting.startDate)
        assertEquals(endDate, jobPosting.endDate)
        assertEquals(requirements, jobPosting.requirements)
        assertEquals(notes, jobPosting.notes)
        assertNull(jobPosting.deletedAt)
        assertNotNull(jobPosting.createdAt)
        assertNotNull(jobPosting.updatedAt)
    }
    
    @Test
    fun `createJobPosting은 nullable 필드들이 null이어도 JobPosting을 생성한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting = repository.createJobPosting(
            companyName = "네이버",
            position = null,
            jobPostingUrl = null,
            startDate = null,
            endDate = null,
            requirements = null,
            notes = null
        )
        
        assertEquals("네이버", jobPosting.companyName)
        assertNull(jobPosting.position)
        assertNull(jobPosting.jobPostingUrl)
        assertNull(jobPosting.startDate)
        assertNull(jobPosting.endDate)
        assertNull(jobPosting.requirements)
        assertNull(jobPosting.notes)
        assertNull(jobPosting.deletedAt)
    }
    
    @Test
    fun `findAll은 삭제되지 않은 모든 JobPosting을 createdAt 역순으로 반환한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting1 = repository.createJobPosting("회사1", "직무1", null, null, null, null, null)
        Thread.sleep(10)
        val jobPosting2 = repository.createJobPosting("회사2", "직무2", null, null, null, null, null)
        Thread.sleep(10)
        val jobPosting3 = repository.createJobPosting("회사3", "직무3", null, null, null, null, null)
        
        repository.deleteJobPosting(jobPosting2.id.value)
        
        val result = repository.findAll()
        
        assertEquals(2, result.size)
        assertEquals("회사3", result[0].companyName)
        assertEquals("회사1", result[1].companyName)
    }
    
    @Test
    fun `findById는 ID로 JobPosting을 조회한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting = repository.createJobPosting("테스트회사", "테스트직무", null, null, null, null, null)
        val jobPostingId = jobPosting.id.value
        
        val found = repository.findById(jobPostingId)
        
        assertNotNull(found)
        assertEquals(jobPostingId, found!!.id.value)
        assertEquals("테스트회사", found.companyName)
        assertEquals("테스트직무", found.position)
    }
    
    @Test
    fun `findById는 삭제된 JobPosting에 대해 null을 반환한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting = repository.createJobPosting("삭제될회사", "삭제될직무", null, null, null, null, null)
        val jobPostingId = jobPosting.id.value
        
        repository.deleteJobPosting(jobPostingId)
        val found = repository.findById(jobPostingId)
        
        assertNull(found)
    }
    
    @Test
    fun `findByCompanyName은 회사명으로 JobPosting을 조회한다`() = withTestTransaction(JobPosting, JobApply) {
        repository.createJobPosting("카카오", "백엔드", null, null, null, null, null)
        repository.createJobPosting("카카오", "프론트엔드", null, null, null, null, null)
        repository.createJobPosting("네이버", "백엔드", null, null, null, null, null)
        
        val result = repository.findByCompanyName("카카오")
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.companyName == "카카오" })
    }
    
    @Test
    fun `findByEndDateRange는 마감일 범위로 JobPosting을 조회한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting1 = repository.createJobPosting("회사1", "직무1", null, null, 
            LocalDate.of(2024, 1, 10), null, null)
        val jobPosting2 = repository.createJobPosting("회사2", "직무2", null, null, 
            LocalDate.of(2024, 1, 20), null, null)
        val jobPosting3 = repository.createJobPosting("회사3", "직무3", null, null, 
            LocalDate.of(2024, 1, 30), null, null)
        
        val result = repository.findByEndDateRange(
            LocalDate.of(2024, 1, 15), 
            LocalDate.of(2024, 1, 25)
        )
        
        assertEquals(1, result.size)
        assertEquals("회사2", result[0].companyName)
    }
    
    @Test
    fun `findUpcomingDeadlines는 향후 N일 내 마감인 JobPosting을 조회한다`() = withTestTransaction(JobPosting, JobApply) {
        val today = LocalDate.now()
        val jobPosting1 = repository.createJobPosting("회사1", "직무1", null, null, 
            today.plusDays(5), null, null)
        val jobPosting2 = repository.createJobPosting("회사2", "직무2", null, null, 
            today.plusDays(15), null, null)
        val jobPosting3 = repository.createJobPosting("회사3", "직무3", null, null, 
            today.minusDays(1), null, null)
        
        val result = repository.findUpcomingDeadlines(10)
        
        assertEquals(1, result.size)
        assertEquals("회사1", result[0].companyName)
    }
    
    @Test
    fun `updateCompanyName은 회사명을 수정한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting = repository.createJobPosting("원래회사", "테스트직무", null, null, null, null, null)
        val jobPostingId = jobPosting.id.value
        val newCompanyName = "수정된회사"
        
        Thread.sleep(10)
        val updated = repository.updateCompanyName(jobPostingId, newCompanyName)
        
        assertTrue(updated)
        val reloadedJobPosting = repository.findById(jobPostingId)
        assertNotNull(reloadedJobPosting)
        assertEquals(newCompanyName, reloadedJobPosting!!.companyName)
    }
    
    @Test
    fun `updateRequirements는 요구사항을 수정한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting = repository.createJobPosting("테스트회사", "테스트직무", null, null, null, null, null)
        val jobPostingId = jobPosting.id.value
        val newRequirements = "자소서 5항목, 코딩테스트"
        
        Thread.sleep(10)
        val updated = repository.updateRequirements(jobPostingId, newRequirements)
        
        assertTrue(updated)
        val reloadedJobPosting = repository.findById(jobPostingId)
        assertNotNull(reloadedJobPosting)
        assertEquals(newRequirements, reloadedJobPosting!!.requirements)
    }
    
    @Test
    fun `moveToJobApply는 JobPosting을 JobApply로 변환한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting = repository.createJobPosting(
            companyName = "테스트회사",
            position = "테스트직무",
            jobPostingUrl = "https://example.com",
            startDate = null,
            endDate = null,
            requirements = "자소서 3항목",
            notes = "좋은 회사"
        )
        val jobPostingId = jobPosting.id.value
        
        val jobApply = repository.moveToJobApply(jobPostingId)
        
        assertNotNull(jobApply)
        assertEquals("테스트회사", jobApply!!.companyName)
        assertEquals("테스트직무", jobApply.position)
        assertEquals("https://example.com", jobApply.jobPostingUrl)
        assertTrue(jobApply.notes!!.contains("요구사항: 자소서 3항목"))
        assertTrue(jobApply.notes!!.contains("메모: 좋은 회사"))
        
        val deletedJobPosting = repository.findById(jobPostingId)
        assertNull(deletedJobPosting)
    }
    
    @Test
    fun `deleteJobPosting은 JobPosting을 soft delete한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting = repository.createJobPosting("삭제될회사", "삭제될직무", null, null, null, null, null)
        val jobPostingId = jobPosting.id.value
        
        val deleted = repository.deleteJobPosting(jobPostingId)
        
        assertTrue(deleted)
        assertNotNull(jobPosting.deletedAt)
        
        val allJobPostings = repository.findAll()
        assertTrue(allJobPostings.none { it.id.value == jobPostingId })
    }
    
    @Test
    fun `deleteJobPostings는 여러 JobPosting을 soft delete한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting1 = repository.createJobPosting("회사1", "직무1", null, null, null, null, null)
        val jobPosting2 = repository.createJobPosting("회사2", "직무2", null, null, null, null, null)
        val jobPosting3 = repository.createJobPosting("회사3", "직무3", null, null, null, null, null)
        
        val deletedCount = repository.deleteJobPostings(listOf(
            jobPosting1.id.value, 
            jobPosting3.id.value
        ))
        
        assertEquals(2, deletedCount)
        
        val remainingJobPostings = repository.findAll()
        assertEquals(1, remainingJobPostings.size)
        assertEquals("회사2", remainingJobPostings[0].companyName)
    }
    
    @Test
    fun `update 메서드들은 삭제된 JobPosting에 대해 false를 반환한다`() = withTestTransaction(JobPosting, JobApply) {
        val jobPosting = repository.createJobPosting("삭제될회사", "삭제될직무", null, null, null, null, null)
        val jobPostingId = jobPosting.id.value
        
        repository.deleteJobPosting(jobPostingId)
        
        assertFalse(repository.updateCompanyName(jobPostingId, "새 회사"))
        assertFalse(repository.updatePosition(jobPostingId, "새 직무"))
        assertFalse(repository.updateRequirements(jobPostingId, "새 요구사항"))
        assertFalse(repository.updateNotes(jobPostingId, "새 메모"))
    }
}