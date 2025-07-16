package com.jobis.mcp

import com.jobis.domain.JobApplyStatus
import com.jobis.repository.JobApplyRepository
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobApplyTools(private val repository: JobApplyRepository) {
    
    fun registerTools(server: Server) {
        registerCreateJobApply(server)
        registerFindAllJobApplies(server)
        registerFindActiveJobApplies(server)
        registerUpdateJobApplyStatus(server)
        registerDeleteJobApply(server)
        registerFindJobAppliesByStatus(server)
        registerFindJobAppliesByCompany(server)
        registerUpdateJobApplyDetails(server)
    }
    
    private fun registerCreateJobApply(server: Server) {
        server.addTool(
            name = "create_job_apply",
            description = "새로운 지원 정보를 생성합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("companyName") {
                        put("type", "string")
                        put("description", "회사명")
                    }
                    putJsonObject("position") {
                        put("type", "string")
                        put("description", "직무 (선택사항)")
                    }
                    putJsonObject("jobPostingUrl") {
                        put("type", "string")
                        put("description", "공고 링크 (선택사항)")
                    }
                    putJsonObject("appliedAt") {
                        put("type", "string")
                        put("description", "지원일 (YYYY-MM-DD HH:mm:ss, 한국 서울 시간 기준으로 입력, 선택사항)")
                    }
                    putJsonObject("nextEventDate") {
                        put("type", "string")
                        put("description", "다음 일정 (YYYY-MM-DD HH:mm:ss, 한국 서울 시간 기준으로 입력, 선택사항)")
                    }
                    putJsonObject("notes") {
                        put("type", "string")
                        put("description", "메모 (선택사항)")
                    }
                },
                required = listOf("companyName")
            )
        ) { request ->
            val companyName = request.arguments["companyName"]?.jsonPrimitive?.content
            if (companyName == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("The 'companyName' parameter is required."))
                )
            }
            
            val position = request.arguments["position"]?.jsonPrimitive?.contentOrNull
            val jobPostingUrl = request.arguments["jobPostingUrl"]?.jsonPrimitive?.contentOrNull
            val appliedAt = request.arguments["appliedAt"]?.jsonPrimitive?.contentOrNull?.let { 
                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
            val nextEventDate = request.arguments["nextEventDate"]?.jsonPrimitive?.contentOrNull?.let { 
                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
            val notes = request.arguments["notes"]?.jsonPrimitive?.contentOrNull
            
            val jobApply = repository.createJobApply(
                companyName = companyName,
                position = position,
                jobPostingUrl = jobPostingUrl,
                appliedAt = appliedAt,
                nextEventDate = nextEventDate,
                notes = notes
            )
            
            val result = buildJsonObject {
                put("success", true)
                putJsonObject("jobApply") {
                    put("id", jobApply.id.value)
                    put("companyName", jobApply.companyName)
                    put("position", jobApply.position)
                    put("jobPostingUrl", jobApply.jobPostingUrl)
                    put("appliedAt", jobApply.appliedAt?.toString())
                    put("status", jobApply.status.name)
                    put("nextEventDate", jobApply.nextEventDate?.toString())
                    put("notes", jobApply.notes)
                    put("createdAt", jobApply.createdAt.toString())
                    put("updatedAt", jobApply.updatedAt.toString())
                }
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerFindAllJobApplies(server: Server) {
        server.addTool(
            name = "find_all_job_applies",
            description = "모든 지원 정보를 조회합니다 (삭제된 항목 제외, 최신순 정렬)",
            inputSchema = Tool.Input(
                properties = buildJsonObject { },
                required = emptyList()
            )
        ) { _ ->
            val jobApplies = repository.findAll()
            
            val result = buildJsonObject {
                put("success", true)
                putJsonArray("jobApplies") {
                    jobApplies.forEach { jobApply ->
                        addJsonObject {
                            put("id", jobApply.id.value)
                            put("companyName", jobApply.companyName)
                            put("position", jobApply.position)
                            put("jobPostingUrl", jobApply.jobPostingUrl)
                            put("appliedAt", jobApply.appliedAt?.toString())
                            put("status", jobApply.status.name)
                            put("nextEventDate", jobApply.nextEventDate?.toString())
                            put("notes", jobApply.notes)
                            put("createdAt", jobApply.createdAt.toString())
                            put("updatedAt", jobApply.updatedAt.toString())
                        }
                    }
                }
                put("count", jobApplies.size)
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerFindActiveJobApplies(server: Server) {
        server.addTool(
            name = "find_active_job_applies",
            description = "진행 중인 지원 정보를 조회합니다 (합격/불합격 제외)",
            inputSchema = Tool.Input(
                properties = buildJsonObject { },
                required = emptyList()
            )
        ) { _ ->
            val jobApplies = repository.findActiveJobApplies()
            
            val result = buildJsonObject {
                put("success", true)
                putJsonArray("jobApplies") {
                    jobApplies.forEach { jobApply ->
                        addJsonObject {
                            put("id", jobApply.id.value)
                            put("companyName", jobApply.companyName)
                            put("position", jobApply.position)
                            put("jobPostingUrl", jobApply.jobPostingUrl)
                            put("appliedAt", jobApply.appliedAt?.toString())
                            put("status", jobApply.status.name)
                            put("nextEventDate", jobApply.nextEventDate?.toString())
                            put("notes", jobApply.notes)
                            put("createdAt", jobApply.createdAt.toString())
                            put("updatedAt", jobApply.updatedAt.toString())
                        }
                    }
                }
                put("count", jobApplies.size)
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerUpdateJobApplyStatus(server: Server) {
        server.addTool(
            name = "update_job_apply_status",
            description = "지원 상태를 업데이트합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("id") {
                        put("type", "integer")
                        put("description", "지원 ID")
                    }
                    putJsonObject("status") {
                        put("type", "string")
                        put("description", "새로운 상태 (APPLIED, EXAM_SCHEDULED, EXAM_RESULT_WAITING, INTERVIEW_SCHEDULED, INTERVIEW_RESULT_WAITING, PASSED, REJECTED)")
                    }
                },
                required = listOf("id", "status")
            )
        ) { request ->
            val id = request.arguments["id"]?.jsonPrimitive?.longOrNull
            val statusStr = request.arguments["status"]?.jsonPrimitive?.content
            
            if (id == null || statusStr == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("Both 'id' and 'status' parameters are required."))
                )
            }
            
            val status = try {
                JobApplyStatus.valueOf(statusStr)
            } catch (e: IllegalArgumentException) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("Invalid status: $statusStr. Valid values: ${JobApplyStatus.values().joinToString(", ")}"))
                )
            }
            
            val updated = repository.updateStatus(id, status)
            if (!updated) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("JobApply not found with id: $id"))
                )
            }
            
            val jobApply = repository.findById(id)!!
            val result = buildJsonObject {
                put("success", true)
                putJsonObject("jobApply") {
                    put("id", jobApply.id.value)
                    put("companyName", jobApply.companyName)
                    put("position", jobApply.position)
                    put("jobPostingUrl", jobApply.jobPostingUrl)
                    put("appliedAt", jobApply.appliedAt?.toString())
                    put("status", jobApply.status.name)
                    put("nextEventDate", jobApply.nextEventDate?.toString())
                    put("notes", jobApply.notes)
                    put("createdAt", jobApply.createdAt.toString())
                    put("updatedAt", jobApply.updatedAt.toString())
                }
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerDeleteJobApply(server: Server) {
        server.addTool(
            name = "delete_job_apply",
            description = "지원 정보를 삭제합니다 (soft delete)",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("id") {
                        put("type", "integer")
                        put("description", "삭제할 지원 ID")
                    }
                },
                required = listOf("id")
            )
        ) { request ->
            val id = request.arguments["id"]?.jsonPrimitive?.longOrNull
            if (id == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("The 'id' parameter is required."))
                )
            }
            
            val deleted = repository.deleteJobApply(id)
            
            val result = buildJsonObject {
                put("success", deleted)
                put("message", if (deleted) "JobApply deleted successfully" else "JobApply not found with id: $id")
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerFindJobAppliesByStatus(server: Server) {
        server.addTool(
            name = "find_job_applies_by_status",
            description = "특정 상태의 지원 정보를 조회합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("status") {
                        put("type", "string")
                        put("description", "조회할 상태 (APPLIED, EXAM_SCHEDULED, EXAM_RESULT_WAITING, INTERVIEW_SCHEDULED, INTERVIEW_RESULT_WAITING, PASSED, REJECTED)")
                    }
                },
                required = listOf("status")
            )
        ) { request ->
            val statusStr = request.arguments["status"]?.jsonPrimitive?.content
            if (statusStr == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("The 'status' parameter is required."))
                )
            }
            
            val status = try {
                JobApplyStatus.valueOf(statusStr)
            } catch (e: IllegalArgumentException) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("Invalid status: $statusStr. Valid values: ${JobApplyStatus.values().joinToString(", ")}"))
                )
            }
            
            val jobApplies = repository.findByStatus(status)
            
            val result = buildJsonObject {
                put("success", true)
                putJsonArray("jobApplies") {
                    jobApplies.forEach { jobApply ->
                        addJsonObject {
                            put("id", jobApply.id.value)
                            put("companyName", jobApply.companyName)
                            put("position", jobApply.position)
                            put("jobPostingUrl", jobApply.jobPostingUrl)
                            put("appliedAt", jobApply.appliedAt?.toString())
                            put("status", jobApply.status.name)
                            put("nextEventDate", jobApply.nextEventDate?.toString())
                            put("notes", jobApply.notes)
                            put("createdAt", jobApply.createdAt.toString())
                            put("updatedAt", jobApply.updatedAt.toString())
                        }
                    }
                }
                put("count", jobApplies.size)
                put("statusFilter", status.name)
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerFindJobAppliesByCompany(server: Server) {
        server.addTool(
            name = "find_job_applies_by_company",
            description = "특정 회사의 지원 정보를 조회합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("companyName") {
                        put("type", "string")
                        put("description", "조회할 회사명")
                    }
                },
                required = listOf("companyName")
            )
        ) { request ->
            val companyName = request.arguments["companyName"]?.jsonPrimitive?.content
            if (companyName == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("The 'companyName' parameter is required."))
                )
            }
            
            val jobApplies = repository.findByCompanyName(companyName)
            
            val result = buildJsonObject {
                put("success", true)
                putJsonArray("jobApplies") {
                    jobApplies.forEach { jobApply ->
                        addJsonObject {
                            put("id", jobApply.id.value)
                            put("companyName", jobApply.companyName)
                            put("position", jobApply.position)
                            put("jobPostingUrl", jobApply.jobPostingUrl)
                            put("appliedAt", jobApply.appliedAt?.toString())
                            put("status", jobApply.status.name)
                            put("nextEventDate", jobApply.nextEventDate?.toString())
                            put("notes", jobApply.notes)
                            put("createdAt", jobApply.createdAt.toString())
                            put("updatedAt", jobApply.updatedAt.toString())
                        }
                    }
                }
                put("count", jobApplies.size)
                put("companyFilter", companyName)
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerUpdateJobApplyDetails(server: Server) {
        server.addTool(
            name = "update_job_apply_details",
            description = "지원 정보의 세부사항을 업데이트합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("id") {
                        put("type", "integer")
                        put("description", "지원 ID")
                    }
                    putJsonObject("position") {
                        put("type", "string")
                        put("description", "직무 (선택사항)")
                    }
                    putJsonObject("jobPostingUrl") {
                        put("type", "string")
                        put("description", "공고 링크 (선택사항)")
                    }
                    putJsonObject("appliedAt") {
                        put("type", "string")
                        put("description", "지원일 (YYYY-MM-DD HH:mm:ss, 한국 서울 시간 기준으로 입력, 선택사항)")
                    }
                    putJsonObject("nextEventDate") {
                        put("type", "string")
                        put("description", "다음 일정 (YYYY-MM-DD HH:mm:ss, 한국 서울 시간 기준으로 입력, 선택사항)")
                    }
                    putJsonObject("notes") {
                        put("type", "string")
                        put("description", "메모 (선택사항)")
                    }
                },
                required = listOf("id")
            )
        ) { request ->
            val id = request.arguments["id"]?.jsonPrimitive?.longOrNull
            if (id == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("The 'id' parameter is required."))
                )
            }
            
            val jobApply = repository.findById(id)
            if (jobApply == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("JobApply not found with id: $id"))
                )
            }
            
            var updated = false
            
            request.arguments["position"]?.jsonPrimitive?.contentOrNull?.let { position ->
                if (repository.updatePosition(id, position)) updated = true
            }
            
            request.arguments["jobPostingUrl"]?.jsonPrimitive?.contentOrNull?.let { jobPostingUrl ->
                if (repository.updateJobPostingUrl(id, jobPostingUrl)) updated = true
            }
            
            request.arguments["appliedAt"]?.jsonPrimitive?.contentOrNull?.let { appliedAtStr ->
                val appliedAt = LocalDateTime.parse(appliedAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                if (repository.updateAppliedAt(id, appliedAt)) updated = true
            }
            
            request.arguments["nextEventDate"]?.jsonPrimitive?.contentOrNull?.let { nextEventDateStr ->
                val nextEventDate = LocalDateTime.parse(nextEventDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                if (repository.updateNextEventDate(id, nextEventDate)) updated = true
            }
            
            request.arguments["notes"]?.jsonPrimitive?.contentOrNull?.let { notes ->
                if (repository.updateNotes(id, notes)) updated = true
            }
            
            val updatedJobApply = repository.findById(id)!!
            
            val result = buildJsonObject {
                put("success", updated)
                putJsonObject("jobApply") {
                    put("id", updatedJobApply.id.value)
                    put("companyName", updatedJobApply.companyName)
                    put("position", updatedJobApply.position)
                    put("jobPostingUrl", updatedJobApply.jobPostingUrl)
                    put("appliedAt", updatedJobApply.appliedAt?.toString())
                    put("status", updatedJobApply.status.name)
                    put("nextEventDate", updatedJobApply.nextEventDate?.toString())
                    put("notes", updatedJobApply.notes)
                    put("createdAt", updatedJobApply.createdAt.toString())
                    put("updatedAt", updatedJobApply.updatedAt.toString())
                }
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
}