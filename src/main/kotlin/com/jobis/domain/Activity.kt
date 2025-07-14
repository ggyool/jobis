package com.jobis.domain

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Activity : LongIdTable("activity") {
    val startedAt = datetime("started_at")
    val endedAt = datetime("ended_at").nullable()
    val description = text("description").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    val deletedAt = datetime("deleted_at").nullable()
}

class ActivityEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ActivityEntity>(Activity)
    
    var startedAt by Activity.startedAt
    var endedAt by Activity.endedAt
    var description by Activity.description
    var createdAt by Activity.createdAt
    var updatedAt by Activity.updatedAt
    var deletedAt by Activity.deletedAt
}