package com.jobis.repository

import com.jobis.domain.JobApply
import com.jobis.domain.JobApplyEntity
import com.jobis.domain.JobApplyStatus
import com.jobis.domain.JobPosting
import com.jobis.domain.JobPostingEntity
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime

class JobPostingRepositoryImpl : JobPostingRepository {
    
    override fun createJobPosting(
        companyName: String,
        position: String?,
        jobPostingUrl: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        requirements: String?,
        notes: String?
    ): JobPostingEntity = transaction {
        val now = LocalDateTime.now()
        JobPostingEntity.new {
            this.companyName = companyName
            this.position = position
            this.jobPostingUrl = jobPostingUrl
            this.startDate = startDate
            this.endDate = endDate
            this.requirements = requirements
            this.notes = notes
            this.createdAt = now
            this.updatedAt = now
        }
    }
    
    override fun findAll(): List<JobPostingEntity> = transaction {
        JobPostingEntity.find { JobPosting.deletedAt.isNull() }
            .orderBy(JobPosting.createdAt to SortOrder.DESC)
            .toList()
    }
    
    override fun findById(id: Long): JobPostingEntity? = transaction {
        JobPostingEntity.findById(id)?.takeIf { it.deletedAt == null }
    }
    
    override fun findByCompanyName(companyName: String): List<JobPostingEntity> = transaction {
        JobPostingEntity.find {
            (JobPosting.companyName eq companyName) and JobPosting.deletedAt.isNull()
        }.orderBy(JobPosting.createdAt to SortOrder.DESC)
        .toList()
    }
    
    override fun findByEndDateRange(startDate: LocalDate, endDate: LocalDate): List<JobPostingEntity> = transaction {
        JobPostingEntity.find {
            (JobPosting.endDate greaterEq startDate) and 
            (JobPosting.endDate lessEq endDate) and
            JobPosting.deletedAt.isNull()
        }.orderBy(JobPosting.endDate to SortOrder.ASC)
        .toList()
    }
    
    override fun findUpcomingDeadlines(days: Int): List<JobPostingEntity> = transaction {
        val today = LocalDate.now()
        val futureDate = today.plusDays(days.toLong())
        
        JobPostingEntity.find {
            (JobPosting.endDate greaterEq today) and 
            (JobPosting.endDate lessEq futureDate) and
            JobPosting.deletedAt.isNull()
        }.orderBy(JobPosting.endDate to SortOrder.ASC)
        .toList()
    }
    
    override fun updateCompanyName(id: Long, companyName: String): Boolean = transaction {
        JobPostingEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobPosting ->
            jobPosting.companyName = companyName
            jobPosting.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updatePosition(id: Long, position: String?): Boolean = transaction {
        JobPostingEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobPosting ->
            jobPosting.position = position
            jobPosting.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateJobPostingUrl(id: Long, jobPostingUrl: String?): Boolean = transaction {
        JobPostingEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobPosting ->
            jobPosting.jobPostingUrl = jobPostingUrl
            jobPosting.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateStartDate(id: Long, startDate: LocalDate?): Boolean = transaction {
        JobPostingEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobPosting ->
            jobPosting.startDate = startDate
            jobPosting.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateEndDate(id: Long, endDate: LocalDate?): Boolean = transaction {
        JobPostingEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobPosting ->
            jobPosting.endDate = endDate
            jobPosting.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateRequirements(id: Long, requirements: String?): Boolean = transaction {
        JobPostingEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobPosting ->
            jobPosting.requirements = requirements
            jobPosting.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateNotes(id: Long, notes: String?): Boolean = transaction {
        JobPostingEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobPosting ->
            jobPosting.notes = notes
            jobPosting.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun moveToJobApply(id: Long): JobApplyEntity? = transaction {
        val jobPosting = JobPostingEntity.findById(id)?.takeIf { it.deletedAt == null }
            ?: return@transaction null
        
        val now = LocalDateTime.now()
        val jobApply = JobApplyEntity.new {
            this.companyName = jobPosting.companyName
            this.position = jobPosting.position
            this.jobPostingUrl = jobPosting.jobPostingUrl
            this.appliedAt = null
            this.status = JobApplyStatus.APPLIED
            this.nextEventDate = null
            this.notes = buildString {
                if (!jobPosting.requirements.isNullOrBlank()) {
                    append("요구사항: ${jobPosting.requirements}")
                }
                if (!jobPosting.notes.isNullOrBlank()) {
                    if (isNotEmpty()) append("\n")
                    append("메모: ${jobPosting.notes}")
                }
            }.takeIf { it.isNotEmpty() }
            this.createdAt = now
            this.updatedAt = now
        }
        
        jobPosting.deletedAt = now
        
        jobApply
    }
    
    override fun deleteJobPosting(id: Long): Boolean = transaction {
        JobPostingEntity.findById(id)?.let { jobPosting ->
            jobPosting.deletedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun deleteJobPostings(ids: List<Long>): Int = transaction {
        val now = LocalDateTime.now()
        var count = 0
        ids.forEach { id ->
            JobPostingEntity.findById(id)?.let { jobPosting ->
                jobPosting.deletedAt = now
                count++
            }
        }
        count
    }
}