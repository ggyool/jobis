package com.jobis.mcp

import com.jobis.repository.JobPostingRepository
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JobPostingTools(private val repository: JobPostingRepository) {

    fun registerTools(server: Server) {
        registerCreateJobPosting(server)
        registerFindAllJobPostings(server)
        registerFindUpcomingDeadlines(server)
        registerUpdateJobPostingDetails(server)
        registerMoveToJobApply(server)
        registerDeleteJobPosting(server)
        registerFindJobPostingsByCompany(server)
    }

    private fun registerCreateJobPosting(server: Server) {
        server.addTool(
            name = "create_job_posting",
            description = "새로운 관심 공고를 등록합니다",
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
                    putJsonObject("startDate") {
                        put("type", "string")
                        put("description", "공고 시작일 (YYYY-MM-DD, 선택사항)")
                    }
                    putJsonObject("endDate") {
                        put("type", "string")
                        put("description", "공고 마감일 (YYYY-MM-DD, 선택사항)")
                    }
                    putJsonObject("requirements") {
                        put("type", "string")
                        put("description", "공고 요구사항 (자소서, 포트폴리오 등 특이사항, 선택사항)")
                    }
                    putJsonObject("notes") {
                        put("type", "string")
                        put("description", "개인 메모 (선택사항)")
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
            val startDate = request.arguments["startDate"]?.jsonPrimitive?.contentOrNull?.let {
                LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }
            val endDate = request.arguments["endDate"]?.jsonPrimitive?.contentOrNull?.let {
                LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }
            val requirements = request.arguments["requirements"]?.jsonPrimitive?.contentOrNull
            val notes = request.arguments["notes"]?.jsonPrimitive?.contentOrNull

            val jobPosting = repository.createJobPosting(
                companyName = companyName,
                position = position,
                jobPostingUrl = jobPostingUrl,
                startDate = startDate,
                endDate = endDate,
                requirements = requirements,
                notes = notes
            )

            val result = buildJsonObject {
                put("success", true)
                putJsonObject("jobPosting") {
                    put("id", jobPosting.id.value)
                    put("companyName", jobPosting.companyName)
                    put("position", jobPosting.position)
                    put("jobPostingUrl", jobPosting.jobPostingUrl)
                    put("startDate", jobPosting.startDate?.toString())
                    put("endDate", jobPosting.endDate?.toString())
                    put("requirements", jobPosting.requirements)
                    put("notes", jobPosting.notes)
                    put("createdAt", jobPosting.createdAt.toString())
                    put("updatedAt", jobPosting.updatedAt.toString())
                }
            }

            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }

    private fun registerFindAllJobPostings(server: Server) {
        server.addTool(
            name = "find_all_job_postings",
            description = "모든 관심 공고를 조회합니다 (최신순 정렬)",
            inputSchema = Tool.Input(
                properties = buildJsonObject { },
                required = emptyList()
            )
        ) { _ ->
            val jobPostings = repository.findAll()

            val result = buildJsonObject {
                put("success", true)
                putJsonArray("jobPostings") {
                    jobPostings.forEach { jobPosting ->
                        addJsonObject {
                            put("id", jobPosting.id.value)
                            put("companyName", jobPosting.companyName)
                            put("position", jobPosting.position)
                            put("jobPostingUrl", jobPosting.jobPostingUrl)
                            put("startDate", jobPosting.startDate?.toString())
                            put("endDate", jobPosting.endDate?.toString())
                            put("requirements", jobPosting.requirements)
                            put("notes", jobPosting.notes)
                            put("createdAt", jobPosting.createdAt.toString())
                            put("updatedAt", jobPosting.updatedAt.toString())
                        }
                    }
                }
                put("count", jobPostings.size)
            }

            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }

    private fun registerFindUpcomingDeadlines(server: Server) {
        server.addTool(
            name = "find_upcoming_deadlines",
            description = "임박한 마감일의 관심 공고를 조회합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("days") {
                        put("type", "integer")
                        put("description", "향후 며칠 내 마감인 공고를 조회할지 (기본값: 7일)")
                    }
                },
                required = emptyList()
            )
        ) { request ->
            val days = request.arguments["days"]?.jsonPrimitive?.longOrNull?.toInt() ?: 7
            val jobPostings = repository.findUpcomingDeadlines(days)

            val result = buildJsonObject {
                put("success", true)
                putJsonArray("jobPostings") {
                    jobPostings.forEach { jobPosting ->
                        addJsonObject {
                            put("id", jobPosting.id.value)
                            put("companyName", jobPosting.companyName)
                            put("position", jobPosting.position)
                            put("jobPostingUrl", jobPosting.jobPostingUrl)
                            put("startDate", jobPosting.startDate?.toString())
                            put("endDate", jobPosting.endDate?.toString())
                            put("requirements", jobPosting.requirements)
                            put("notes", jobPosting.notes)
                            put("createdAt", jobPosting.createdAt.toString())
                            put("updatedAt", jobPosting.updatedAt.toString())
                        }
                    }
                }
                put("count", jobPostings.size)
                put("daysFilter", days)
            }

            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }

    private fun registerUpdateJobPostingDetails(server: Server) {
        server.addTool(
            name = "update_job_posting_details",
            description = "관심 공고의 세부사항을 업데이트합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("id") {
                        put("type", "integer")
                        put("description", "관심 공고 ID")
                    }
                    putJsonObject("position") {
                        put("type", "string")
                        put("description", "직무 (선택사항)")
                    }
                    putJsonObject("jobPostingUrl") {
                        put("type", "string")
                        put("description", "공고 링크 (선택사항)")
                    }
                    putJsonObject("startDate") {
                        put("type", "string")
                        put("description", "공고 시작일 (YYYY-MM-DD, 선택사항)")
                    }
                    putJsonObject("endDate") {
                        put("type", "string")
                        put("description", "공고 마감일 (YYYY-MM-DD, 선택사항)")
                    }
                    putJsonObject("requirements") {
                        put("type", "string")
                        put("description", "공고 요구사항 (선택사항)")
                    }
                    putJsonObject("notes") {
                        put("type", "string")
                        put("description", "개인 메모 (선택사항)")
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

            val jobPosting = repository.findById(id)
            if (jobPosting == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("JobPosting not found with id: $id"))
                )
            }

            var updated = false

            request.arguments["position"]?.jsonPrimitive?.contentOrNull?.let { position ->
                if (repository.updatePosition(id, position)) updated = true
            }

            request.arguments["jobPostingUrl"]?.jsonPrimitive?.contentOrNull?.let { jobPostingUrl ->
                if (repository.updateJobPostingUrl(id, jobPostingUrl)) updated = true
            }

            request.arguments["startDate"]?.jsonPrimitive?.contentOrNull?.let { startDateStr ->
                val startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                if (repository.updateStartDate(id, startDate)) updated = true
            }

            request.arguments["endDate"]?.jsonPrimitive?.contentOrNull?.let { endDateStr ->
                val endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                if (repository.updateEndDate(id, endDate)) updated = true
            }

            request.arguments["requirements"]?.jsonPrimitive?.contentOrNull?.let { requirements ->
                if (repository.updateRequirements(id, requirements)) updated = true
            }

            request.arguments["notes"]?.jsonPrimitive?.contentOrNull?.let { notes ->
                if (repository.updateNotes(id, notes)) updated = true
            }

            val updatedJobPosting = repository.findById(id)!!

            val result = buildJsonObject {
                put("success", updated)
                putJsonObject("jobPosting") {
                    put("id", updatedJobPosting.id.value)
                    put("companyName", updatedJobPosting.companyName)
                    put("position", updatedJobPosting.position)
                    put("jobPostingUrl", updatedJobPosting.jobPostingUrl)
                    put("startDate", updatedJobPosting.startDate?.toString())
                    put("endDate", updatedJobPosting.endDate?.toString())
                    put("requirements", updatedJobPosting.requirements)
                    put("notes", updatedJobPosting.notes)
                    put("createdAt", updatedJobPosting.createdAt.toString())
                    put("updatedAt", updatedJobPosting.updatedAt.toString())
                }
            }

            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }

    private fun registerMoveToJobApply(server: Server) {
        server.addTool(
            name = "move_to_job_apply",
            description = "관심 공고를 지원 현황으로 이동합니다 (관심 공고는 삭제되고 지원 현황에 추가됩니다)",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("id") {
                        put("type", "integer")
                        put("description", "이동할 관심 공고 ID")
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

            val jobApply = repository.moveToJobApply(id)
            if (jobApply == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("JobPosting not found with id: $id"))
                )
            }

            val result = buildJsonObject {
                put("success", true)
                put("message", "JobPosting moved to JobApply successfully")
                putJsonObject("jobApply") {
                    put("id", jobApply.id.value)
                    put("companyName", jobApply.companyName)
                    put("position", jobApply.position)
                    put("jobPostingUrl", jobApply.jobPostingUrl)
                    put("appliedAt", jobApply.appliedAt?.toString())
                    put("status", jobApply.status.description)
                    put("nextEventDate", jobApply.nextEventDate?.toString())
                    put("notes", jobApply.notes)
                    put("createdAt", jobApply.createdAt.toString())
                    put("updatedAt", jobApply.updatedAt.toString())
                }
            }

            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }

    private fun registerDeleteJobPosting(server: Server) {
        server.addTool(
            name = "delete_job_posting",
            description = "관심 공고를 삭제합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("id") {
                        put("type", "integer")
                        put("description", "삭제할 관심 공고 ID")
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

            val deleted = repository.deleteJobPosting(id)

            val result = buildJsonObject {
                put("success", deleted)
                put(
                    "message",
                    if (deleted) "JobPosting deleted successfully" else "JobPosting not found with id: $id"
                )
            }

            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }

    private fun registerFindJobPostingsByCompany(server: Server) {
        server.addTool(
            name = "find_job_postings_by_company",
            description = "특정 회사의 관심 공고를 조회합니다",
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

            val jobPostings = repository.findByCompanyName(companyName)

            val result = buildJsonObject {
                put("success", true)
                putJsonArray("jobPostings") {
                    jobPostings.forEach { jobPosting ->
                        addJsonObject {
                            put("id", jobPosting.id.value)
                            put("companyName", jobPosting.companyName)
                            put("position", jobPosting.position)
                            put("jobPostingUrl", jobPosting.jobPostingUrl)
                            put("startDate", jobPosting.startDate?.toString())
                            put("endDate", jobPosting.endDate?.toString())
                            put("requirements", jobPosting.requirements)
                            put("notes", jobPosting.notes)
                            put("createdAt", jobPosting.createdAt.toString())
                            put("updatedAt", jobPosting.updatedAt.toString())
                        }
                    }
                }
                put("count", jobPostings.size)
                put("companyFilter", companyName)
            }

            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
}