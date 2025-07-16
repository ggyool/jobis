package com.jobis.repository

import com.jobis.domain.JobApplyEntity
import com.jobis.domain.JobApplyStatus
import java.time.LocalDate
import java.time.LocalDateTime

interface JobApplyRepository {

    fun createJobApply(
        companyName: String,
        position: String?,
        jobPostingUrl: String?,
        appliedAt: LocalDateTime?,
        nextEventDate: LocalDateTime?,
        notes: String?
    ): JobApplyEntity

    fun findAll(): List<JobApplyEntity>
    
    fun findActiveJobApplies(): List<JobApplyEntity>
    
    fun findById(id: Long): JobApplyEntity?
    
    fun findByStatus(status: JobApplyStatus): List<JobApplyEntity>
    
    fun findByCompanyName(companyName: String): List<JobApplyEntity>
    
    fun findByAppliedDateRange(startDate: LocalDate, endDate: LocalDate): List<JobApplyEntity>
    
    fun updateStatus(id: Long, status: JobApplyStatus): Boolean
    
    fun updateAppliedAt(id: Long, appliedAt: LocalDateTime?): Boolean
    
    fun updateNextEventDate(id: Long, nextEventDate: LocalDateTime?): Boolean
    
    fun updateNotes(id: Long, notes: String?): Boolean
    
    fun updateCompanyName(id: Long, companyName: String): Boolean
    
    fun updatePosition(id: Long, position: String?): Boolean
    
    fun updateJobPostingUrl(id: Long, jobPostingUrl: String?): Boolean
    
    fun deleteJobApply(id: Long): Boolean
    
    fun deleteJobApplies(ids: List<Long>): Int
}