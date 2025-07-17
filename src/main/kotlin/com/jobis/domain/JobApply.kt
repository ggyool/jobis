package com.jobis.domain

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

enum class JobApplyStatus(val description: String) {
    APPLIED("지원 완료"),
    EXAM_SCHEDULED("시험 예정"),
    EXAM_RESULT_WAITING("시험 결과 대기"),
    INTERVIEW_SCHEDULED("면접 예정"),
    INTERVIEW_RESULT_WAITING("면접 결과 대기"),
    PASSED("합격"),
    REJECTED("불합격")
}

object JobApply : LongIdTable("job_apply") {
    val companyName = varchar("company_name", 50)
    val position = varchar("position", 50).nullable()
    val jobPostingUrl = text("job_posting_url").nullable()
    val appliedAt = datetime("applied_at").nullable()
    val status = enumerationByName<JobApplyStatus>("status", 50)
    val nextEventDate = datetime("next_event_date").nullable()
    val notes = text("notes").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    val deletedAt = datetime("deleted_at").nullable()
}

class JobApplyEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<JobApplyEntity>(JobApply)

    var companyName by JobApply.companyName
    var position by JobApply.position
    var jobPostingUrl by JobApply.jobPostingUrl
    var appliedAt by JobApply.appliedAt
    var status by JobApply.status
    var nextEventDate by JobApply.nextEventDate
    var notes by JobApply.notes
    var createdAt by JobApply.createdAt
    var updatedAt by JobApply.updatedAt
    var deletedAt by JobApply.deletedAt
}