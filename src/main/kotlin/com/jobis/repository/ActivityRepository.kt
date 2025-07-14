package com.jobis.repository

import com.jobis.domain.ActivityEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

interface ActivityRepository {
    fun createActivity(startedAt: LocalDateTime, description: String?): ActivityEntity
    
    fun findAll(): List<ActivityEntity>
    
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<ActivityEntity>
    
    fun updateActivities(activities: List<ActivityEntity>): List<ActivityEntity>
    
    fun updateActivity(activity: ActivityEntity): ActivityEntity
    
    fun deleteActivities(ids: List<Long>): Int
    
    fun deleteActivity(id: Long): Boolean

    fun getTotalDurationByDate(date: LocalDate): Duration
    
    fun getTotalDurationByDateRange(startDate: LocalDate, endDate: LocalDate): Duration
}