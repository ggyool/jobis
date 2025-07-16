package com.jobis.mcp

import com.jobis.repository.ActivityRepositoryImpl
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
import org.slf4j.LoggerFactory

class JobisServer {
    private val logger = LoggerFactory.getLogger(JobisServer::class.java)
    
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
        val repository = ActivityRepositoryImpl()
        val activityTools = ActivityTools(repository)
        activityTools.registerTools(server)
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