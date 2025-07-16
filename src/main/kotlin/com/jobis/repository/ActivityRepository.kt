package com.jobis.repository

import com.jobis.domain.ActivityEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

interface ActivityRepository {

    fun createActivity(description: String?): ActivityEntity
    
    fun findAll(): List<ActivityEntity>

    fun findById(id: Long): ActivityEntity?
    
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<ActivityEntity>

    fun updateStartedAt(id: Long, startedAt: LocalDateTime): Boolean
    
    fun updateEndedAt(id: Long, endedAt: LocalDateTime?): Boolean
    
    fun updateDescription(id: Long, description: String?): Boolean
    
    fun deleteActivities(ids: List<Long>): Int
    
    fun deleteActivity(id: Long): Boolean

    fun getTotalDurationByDate(date: LocalDate): Duration
    
    fun getTotalDurationByDateRange(startDate: LocalDate, endDate: LocalDate): Duration
}