package com.jobis

import com.jobis.config.DatabaseConfig
import com.jobis.domain.Activity
import com.jobis.domain.JobApply
import com.jobis.mcp.JobisServer

fun main() {
    DatabaseConfig.connect()
    DatabaseConfig.createTables(Activity, JobApply)

    val server = JobisServer()
    server.start()
}