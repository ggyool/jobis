package com.jobis.domain

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object JobPosting : LongIdTable("job_posting") {
    val companyName = varchar("company_name", 255)
    val position = varchar("position", 255).nullable()
    val jobPostingUrl = text("job_posting_url").nullable()
    val startDate = date("start_date").nullable()
    val endDate = date("end_date").nullable()
    val requirements = text("requirements").nullable()
    val notes = text("notes").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    val deletedAt = datetime("deleted_at").nullable()
}

class JobPostingEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<JobPostingEntity>(JobPosting)
    
    var companyName by JobPosting.companyName
    var position by JobPosting.position
    var jobPostingUrl by JobPosting.jobPostingUrl
    var startDate by JobPosting.startDate
    var endDate by JobPosting.endDate
    var requirements by JobPosting.requirements
    var notes by JobPosting.notes
    var createdAt by JobPosting.createdAt
    var updatedAt by JobPosting.updatedAt
    var deletedAt by JobPosting.deletedAt
}