package com.jobis.mcp

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TimeTools {

    fun registerTools(server: Server) {
        registerGetCurrentTime(server)
    }

    private fun registerGetCurrentTime(server: Server) {
        server.addTool(
            name = "get_current_time",
            description = "현재 시간을 조회합니다 (한국 서울 시간 기준)",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("format") {
                        put("type", "string")
                        put("description", "시간 형식 (선택사항, 기본값: yyyy-MM-dd HH:mm:ss)")
                    }
                },
                required = emptyList()
            )
        ) { request ->
            val format =
                request.arguments["format"]?.jsonPrimitive?.content ?: "yyyy-MM-dd HH:mm:ss"

            val seoulTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
            val formatter = DateTimeFormatter.ofPattern(format)
            val formattedTime = seoulTime.format(formatter)

            val result = buildJsonObject {
                put("success", true)
                put("currentTime", formattedTime)
                put("timezone", "Asia/Seoul")
                put("timestamp", seoulTime.toInstant().epochSecond)
                put("format", format)
            }

            CallToolResult(content = listOf(TextContent(result.toString())))
        }
    }
}