package com.jobis.config

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseConfig {
    
    fun connect() {
        val profile = System.getProperty("profile") ?: "local"
        when (profile) {
            "test" -> connectInMemory()
            "local" -> connectLocal()
            else -> connectLocal()
        }
    }
    
    fun createTables(vararg tables: Table) {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*tables)
        }
    }
    
    private fun connectLocal() {
        val homeDir = System.getProperty("user.home")
        val jobisDir = File(homeDir, "jobis")

        if (!jobisDir.exists()) {
            jobisDir.mkdirs()
            jobisDir.setReadable(true, false)
            jobisDir.setWritable(true, false)
            jobisDir.setExecutable(true, false)
        }
        
        val dbFile = File(jobisDir, "jobis.db")
        val dbPath = dbFile.absolutePath
        
        Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")

        if (dbFile.exists()) {
            dbFile.setReadable(true, false)
            dbFile.setWritable(true, false)
        }
    }
    
    private fun connectInMemory() {
        Database.connect("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
    }
}