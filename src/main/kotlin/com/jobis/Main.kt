package com.jobis

import com.jobis.config.DatabaseConfig
import com.jobis.domain.Activity
import com.jobis.domain.JobApply
import com.jobis.domain.JobPosting
import com.jobis.mcp.JobisServer

fun main() {
    DatabaseConfig.connect()
    DatabaseConfig.createTables(Activity, JobApply, JobPosting)

    val server = JobisServer()
    server.start()
}