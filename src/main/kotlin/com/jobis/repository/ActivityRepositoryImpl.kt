package com.jobis.repository

import com.jobis.domain.Activity
import com.jobis.domain.ActivityEntity
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class ActivityRepositoryImpl : ActivityRepository {
    
    override fun createActivity(startedAt: LocalDateTime, description: String?): ActivityEntity = transaction {
        val now = LocalDateTime.now()
        ActivityEntity.new {
            this.startedAt = startedAt
            this.description = description
            this.createdAt = now
            this.updatedAt = now
        }
    }
    
    override fun findAll(): List<ActivityEntity> = transaction {
        ActivityEntity.find { Activity.deletedAt.isNull() }
            .orderBy(Activity.startedAt to SortOrder.ASC)
            .toList()
    }
    
    override fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<ActivityEntity> = transaction {
        ActivityEntity.find {
            (Activity.startedAt.date() greaterEq startDate) and 
            (Activity.startedAt.date() lessEq endDate) and
            Activity.deletedAt.isNull()
        }.orderBy(Activity.startedAt to SortOrder.ASC)
        .toList()
    }
    
    override fun updateActivities(activities: List<ActivityEntity>): List<ActivityEntity> = transaction {
        val now = LocalDateTime.now()
        activities.forEach { activity ->
            activity.updatedAt = now
        }
        activities
    }
    
    override fun updateActivity(activity: ActivityEntity): ActivityEntity = transaction {
        activity.updatedAt = LocalDateTime.now()
        activity
    }
    
    override fun deleteActivities(ids: List<Long>): Int = transaction {
        val now = LocalDateTime.now()
        var count = 0
        ids.forEach { id ->
            ActivityEntity.findById(id)?.let { activity ->
                activity.deletedAt = now
                count++
            }
        }
        count
    }
    
    override fun deleteActivity(id: Long): Boolean = transaction {
        ActivityEntity.findById(id)?.let { activity ->
            activity.deletedAt = LocalDateTime.now()
            true
        } ?: false
    }
    
    override fun getTotalDurationByDate(date: LocalDate): Duration = transaction {
        val activities = ActivityEntity.find {
            (Activity.startedAt.date() eq date) and 
            Activity.endedAt.isNotNull() and
            Activity.deletedAt.isNull()
        }
        
        activities.fold(Duration.ZERO) { total, activity ->
            if (activity.endedAt != null) {
                total.plus(Duration.between(activity.startedAt, activity.endedAt))
            } else {
                total
            }
        }
    }
    
    override fun getTotalDurationByDateRange(startDate: LocalDate, endDate: LocalDate): Duration = transaction {
        val activities = ActivityEntity.find {
            (Activity.startedAt.date() greaterEq startDate) and 
            (Activity.startedAt.date() lessEq endDate) and
            Activity.endedAt.isNotNull() and
            Activity.deletedAt.isNull()
        }
        
        activities.fold(Duration.ZERO) { total, activity ->
            if (activity.endedAt != null) {
                total.plus(Duration.between(activity.startedAt, activity.endedAt))
            } else {
                total
            }
        }
    }
}