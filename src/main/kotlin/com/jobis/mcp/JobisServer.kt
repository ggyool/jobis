package com.jobis.mcp

import com.jobis.repository.ActivityRepositoryImpl
import com.jobis.repository.JobApplyRepositoryImpl
import com.jobis.repository.JobPostingRepositoryImpl
import io.ktor.utils.io.streams.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered

class JobisServer {

    fun start() {
        val server = createServer()
        registerTools(server)
        startServer(server)
    }

    private fun createServer(): Server {
        return Server(
            Implementation(
                name = "jobis",
                version = "1.0.0"
            ),
            ServerOptions(
                capabilities = ServerCapabilities(
                    tools = ServerCapabilities.Tools(listChanged = true)
                )
            )
        )
    }

    private fun registerTools(server: Server) {
        val activityRepository = ActivityRepositoryImpl()
        val activityTools = ActivityTools(activityRepository)
        activityTools.registerTools(server)
        
        val jobApplyRepository = JobApplyRepositoryImpl()
        val jobApplyTools = JobApplyTools(jobApplyRepository)
        jobApplyTools.registerTools(server)
        
        val jobPostingRepository = JobPostingRepositoryImpl()
        val jobPostingTools = JobPostingTools(jobPostingRepository)
        jobPostingTools.registerTools(server)
        
        val timeTools = TimeTools()
        timeTools.registerTools(server)
    }

    private fun startServer(server: Server) {

        val transport = StdioServerTransport(
            System.`in`.asInput(),
            System.out.asSink().buffered()
        )

        runBlocking {
            server.connect(transport)
            val done = Job()
            server.onClose {
                done.complete()
            }
            done.join()
        }
    }
}