package com.jobis.mcp

import com.jobis.repository.ActivityRepository
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ActivityTools(private val repository: ActivityRepository) {
    
    fun registerTools(server: Server) {
        registerCreateActivity(server)
        registerFindAllActivities(server)
        registerFindActivitiesByDateRange(server)
        registerUpdateActivity(server)
        registerDeleteActivity(server)
        registerGetTotalDurationByDate(server)
        registerGetTotalDurationByDateRange(server)
    }
    
    private fun registerCreateActivity(server: Server) {
        server.addTool(
            name = "create_activity",
            description = "새로운 활동을 생성합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("startedAt") {
                        put("type", "string")
                        put("description", "시작 시간 (YYYY-MM-DD HH:mm:ss)")
                    }
                    putJsonObject("description") {
                        put("type", "string")
                        put("description", "활동 설명 (선택사항)")
                    }
                },
                required = listOf("startedAt")
            )
        ) { request ->
            val startedAt = request.arguments["startedAt"]?.jsonPrimitive?.content
            if (startedAt == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("The 'startedAt' parameter is required."))
                )
            }
            
            val description = request.arguments["description"]?.jsonPrimitive?.contentOrNull
            
            val activity = repository.createActivity(
                LocalDateTime.parse(startedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                description
            )
            
            val result = buildJsonObject {
                put("success", true)
                putJsonObject("activity") {
                    put("id", activity.id.value)
                    put("startedAt", activity.startedAt.toString())
                    put("endedAt", activity.endedAt?.toString())
                    put("description", activity.description)
                    put("createdAt", activity.createdAt.toString())
                    put("updatedAt", activity.updatedAt.toString())
                }
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerFindAllActivities(server: Server) {
        server.addTool(
            name = "find_all_activities",
            description = "모든 활동을 조회합니다 (삭제된 항목 제외, 시작 시간순 정렬)",
            inputSchema = Tool.Input(
                properties = buildJsonObject { },
                required = emptyList()
            )
        ) { _ ->
            val activities = repository.findAll()
            
            val result = buildJsonObject {
                put("success", true)
                putJsonArray("activities") {
                    activities.forEach { activity ->
                        addJsonObject {
                            put("id", activity.id.value)
                            put("startedAt", activity.startedAt.toString())
                            put("endedAt", activity.endedAt?.toString())
                            put("description", activity.description)
                            put("createdAt", activity.createdAt.toString())
                            put("updatedAt", activity.updatedAt.toString())
                        }
                    }
                }
                put("count", activities.size)
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerFindActivitiesByDateRange(server: Server) {
        server.addTool(
            name = "find_activities_by_date_range",
            description = "날짜 범위로 활동을 조회합니다",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("startDate") {
                        put("type", "string")
                        put("description", "시작 날짜 (YYYY-MM-DD)")
                    }
                    putJsonObject("endDate") {
                        put("type", "string")
                        put("description", "종료 날짜 (YYYY-MM-DD)")
                    }
                },
                required = listOf("startDate", "endDate")
            )
        ) { request ->
            val startDate = request.arguments["startDate"]?.jsonPrimitive?.content
            val endDate = request.arguments["endDate"]?.jsonPrimitive?.content
            
            if (startDate == null || endDate == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("Both 'startDate' and 'endDate' parameters are required."))
                )
            }
            
            val activities = repository.findByDateRange(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
            )
            
            val result = buildJsonObject {
                put("success", true)
                putJsonArray("activities") {
                    activities.forEach { activity ->
                        addJsonObject {
                            put("id", activity.id.value)
                            put("startedAt", activity.startedAt.toString())
                            put("endedAt", activity.endedAt?.toString())
                            put("description", activity.description)
                            put("createdAt", activity.createdAt.toString())
                            put("updatedAt", activity.updatedAt.toString())
                        }
                    }
                }
                put("count", activities.size)
                putJsonObject("dateRange") {
                    put("startDate", startDate)
                    put("endDate", endDate)
                }
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerUpdateActivity(server: Server) {
        server.addTool(
            name = "update_activity",
            description = "활동을 수정합니다 (ID로 조회 후 수정)",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("id") {
                        put("type", "integer")
                        put("description", "활동 ID")
                    }
                    putJsonObject("startedAt") {
                        put("type", "string")
                        put("description", "시작 시간 (YYYY-MM-DD HH:mm:ss)")
                    }
                    putJsonObject("endedAt") {
                        put("type", "string")
                        put("description", "종료 시간 (YYYY-MM-DD HH:mm:ss, 선택사항)")
                    }
                    putJsonObject("description") {
                        put("type", "string")
                        put("description", "활동 설명 (선택사항)")
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
            
            val activities = repository.findAll()
            val activity = activities.find { it.id.value == id }
            if (activity == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("Activity not found with id: $id"))
                )
            }
            
            request.arguments["startedAt"]?.jsonPrimitive?.contentOrNull?.let { startedAtStr ->
                activity.startedAt = LocalDateTime.parse(startedAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
            
            request.arguments["endedAt"]?.jsonPrimitive?.contentOrNull?.let { endedAtStr ->
                activity.endedAt = LocalDateTime.parse(endedAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
            
            request.arguments["description"]?.jsonPrimitive?.contentOrNull?.let { desc ->
                activity.description = desc
            }
            
            val updatedActivity = repository.updateActivity(activity)
            
            val result = buildJsonObject {
                put("success", true)
                putJsonObject("activity") {
                    put("id", updatedActivity.id.value)
                    put("startedAt", updatedActivity.startedAt.toString())
                    put("endedAt", updatedActivity.endedAt?.toString())
                    put("description", updatedActivity.description)
                    put("createdAt", updatedActivity.createdAt.toString())
                    put("updatedAt", updatedActivity.updatedAt.toString())
                }
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerDeleteActivity(server: Server) {
        server.addTool(
            name = "delete_activity",
            description = "활동을 삭제합니다 (soft delete)",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("id") {
                        put("type", "integer")
                        put("description", "삭제할 활동 ID")
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
            
            val deleted = repository.deleteActivity(id)
            
            val result = buildJsonObject {
                put("success", deleted)
                put("message", if (deleted) "Activity deleted successfully" else "Activity not found with id: $id")
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerGetTotalDurationByDate(server: Server) {
        server.addTool(
            name = "get_total_duration_by_date",
            description = "특정 날짜의 총 소요시간을 계산합니다 (완료된 활동만)",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("date") {
                        put("type", "string")
                        put("description", "날짜 (YYYY-MM-DD)")
                    }
                },
                required = listOf("date")
            )
        ) { request ->
            val dateStr = request.arguments["date"]?.jsonPrimitive?.content
            if (dateStr == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("The 'date' parameter is required."))
                )
            }
            
            val date = LocalDate.parse(dateStr)
            val duration = repository.getTotalDurationByDate(date)
            
            val result = buildJsonObject {
                put("success", true)
                put("date", date.toString())
                putJsonObject("totalDuration") {
                    put("hours", duration.toHours())
                    put("minutes", duration.toMinutes() % 60)
                    put("totalMinutes", duration.toMinutes())
                    put("iso8601", duration.toString())
                }
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
    
    private fun registerGetTotalDurationByDateRange(server: Server) {
        server.addTool(
            name = "get_total_duration_by_date_range",
            description = "날짜 범위의 총 소요시간을 계산합니다 (완료된 활동만)",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("startDate") {
                        put("type", "string")
                        put("description", "시작 날짜 (YYYY-MM-DD)")
                    }
                    putJsonObject("endDate") {
                        put("type", "string")
                        put("description", "종료 날짜 (YYYY-MM-DD)")
                    }
                },
                required = listOf("startDate", "endDate")
            )
        ) { request ->
            val startDateStr = request.arguments["startDate"]?.jsonPrimitive?.content
            val endDateStr = request.arguments["endDate"]?.jsonPrimitive?.content
            
            if (startDateStr == null || endDateStr == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("Both 'startDate' and 'endDate' parameters are required."))
                )
            }
            
            val startDate = LocalDate.parse(startDateStr)
            val endDate = LocalDate.parse(endDateStr)
            val duration = repository.getTotalDurationByDateRange(startDate, endDate)
            
            val result = buildJsonObject {
                put("success", true)
                putJsonObject("dateRange") {
                    put("startDate", startDate.toString())
                    put("endDate", endDate.toString())
                }
                putJsonObject("totalDuration") {
                    put("hours", duration.toHours())
                    put("minutes", duration.toMinutes() % 60)
                    put("totalMinutes", duration.toMinutes())
                    put("iso8601", duration.toString())
                }
            }
            
            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
}