package com.jobis.repository

import com.jobis.domain.JobApply
import com.jobis.domain.JobApplyEntity
import com.jobis.domain.JobApplyStatus
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime

class JobApplyRepositoryImpl : JobApplyRepository {
    
    override fun createJobApply(
        companyName: String,
        position: String?,
        jobPostingUrl: String?,
        appliedAt: LocalDateTime?,
        nextEventDate: LocalDateTime?,
        notes: String?
    ): JobApplyEntity = transaction {
        val now = LocalDateTime.now()
        JobApplyEntity.new {
            this.companyName = companyName
            this.position = position
            this.jobPostingUrl = jobPostingUrl
            this.appliedAt = appliedAt
            this.status = JobApplyStatus.APPLIED
            this.nextEventDate = nextEventDate
            this.notes = notes
            this.createdAt = now
            this.updatedAt = now
        }
    }
    
    override fun findAll(): List<JobApplyEntity> = transaction {
        JobApplyEntity.find { JobApply.deletedAt.isNull() }
            .orderBy(JobApply.createdAt to SortOrder.DESC)
            .toList()
    }
    
    override fun findActiveJobApplies(): List<JobApplyEntity> = transaction {
        JobApplyEntity.find { 
            JobApply.deletedAt.isNull() and 
            (JobApply.status neq JobApplyStatus.PASSED) and 
            (JobApply.status neq JobApplyStatus.REJECTED)
        }.orderBy(JobApply.createdAt to SortOrder.DESC)
        .toList()
    }
    
    override fun findById(id: Long): JobApplyEntity? = transaction {
        JobApplyEntity.findById(id)?.takeIf { it.deletedAt == null }
    }
    
    override fun findByStatus(status: JobApplyStatus): List<JobApplyEntity> = transaction {
        JobApplyEntity.find {
            (JobApply.status eq status) and JobApply.deletedAt.isNull()
        }.orderBy(JobApply.createdAt to SortOrder.DESC)
        .toList()
    }
    
    override fun findByCompanyName(companyName: String): List<JobApplyEntity> = transaction {
        JobApplyEntity.find {
            (JobApply.companyName eq companyName) and JobApply.deletedAt.isNull()
        }.orderBy(JobApply.createdAt to SortOrder.DESC)
        .toList()
    }
    
    override fun findByAppliedDateRange(startDate: LocalDate, endDate: LocalDate): List<JobApplyEntity> = transaction {
        JobApplyEntity.find {
            (JobApply.appliedAt.date() greaterEq startDate) and 
            (JobApply.appliedAt.date() lessEq endDate) and
            JobApply.deletedAt.isNull()
        }.orderBy(JobApply.appliedAt to SortOrder.DESC)
        .toList()
    }
    
    override fun updateStatus(id: Long, status: JobApplyStatus): Boolean = transaction {
        JobApplyEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobApply ->
            jobApply.status = status
            jobApply.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateAppliedAt(id: Long, appliedAt: LocalDateTime?): Boolean = transaction {
        JobApplyEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobApply ->
            jobApply.appliedAt = appliedAt
            jobApply.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateNextEventDate(id: Long, nextEventDate: LocalDateTime?): Boolean = transaction {
        JobApplyEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobApply ->
            jobApply.nextEventDate = nextEventDate
            jobApply.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateNotes(id: Long, notes: String?): Boolean = transaction {
        JobApplyEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobApply ->
            jobApply.notes = notes
            jobApply.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateCompanyName(id: Long, companyName: String): Boolean = transaction {
        JobApplyEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobApply ->
            jobApply.companyName = companyName
            jobApply.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updatePosition(id: Long, position: String?): Boolean = transaction {
        JobApplyEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobApply ->
            jobApply.position = position
            jobApply.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun updateJobPostingUrl(id: Long, jobPostingUrl: String?): Boolean = transaction {
        JobApplyEntity.findById(id)?.takeIf { it.deletedAt == null }?.let { jobApply ->
            jobApply.jobPostingUrl = jobPostingUrl
            jobApply.updatedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun deleteJobApply(id: Long): Boolean = transaction {
        JobApplyEntity.findById(id)?.let { jobApply ->
            jobApply.deletedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun deleteJobApplies(ids: List<Long>): Int = transaction {
        val now = LocalDateTime.now()
        var count = 0
        ids.forEach { id ->
            JobApplyEntity.findById(id)?.let { jobApply ->
                jobApply.deletedAt = now
                count++
            }
        }
        count
    }
}