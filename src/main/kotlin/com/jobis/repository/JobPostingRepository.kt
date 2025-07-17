package com.jobis.repository

import com.jobis.domain.JobApplyEntity
import com.jobis.domain.JobPostingEntity
import java.time.LocalDate

interface JobPostingRepository {
    
    fun createJobPosting(
        companyName: String,
        position: String?,
        jobPostingUrl: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        requirements: String?,
        notes: String?
    ): JobPostingEntity
    
    fun findAll(): List<JobPostingEntity>
    
    fun findById(id: Long): JobPostingEntity?
    
    fun findByCompanyName(companyName: String): List<JobPostingEntity>
    
    fun findByEndDateRange(startDate: LocalDate, endDate: LocalDate): List<JobPostingEntity>
    
    fun findUpcomingDeadlines(days: Int): List<JobPostingEntity>
    
    fun updateCompanyName(id: Long, companyName: String): Boolean
    
    fun updatePosition(id: Long, position: String?): Boolean
    
    fun updateJobPostingUrl(id: Long, jobPostingUrl: String?): Boolean
    
    fun updateStartDate(id: Long, startDate: LocalDate?): Boolean
    
    fun updateEndDate(id: Long, endDate: LocalDate?): Boolean
    
    fun updateRequirements(id: Long, requirements: String?): Boolean
    
    fun updateNotes(id: Long, notes: String?): Boolean
    
    fun moveToJobApply(id: Long): JobApplyEntity?
    
    fun deleteJobPosting(id: Long): Boolean
    
    fun deleteJobPostings(ids: List<Long>): Int
}