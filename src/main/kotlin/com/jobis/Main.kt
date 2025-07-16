package com.jobis

import com.jobis.config.DatabaseConfig
import com.jobis.domain.Activity
import com.jobis.mcp.JobisServer

fun main() {
    DatabaseConfig.connect()
    DatabaseConfig.createTables(Activity)

    val server = JobisServer()
    server.start()
}