package com.jobis.config

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

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
        Database.connect("jdbc:sqlite:jobis.db", driver = "org.sqlite.JDBC")
    }
    
    private fun connectInMemory() {
        Database.connect("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
    }
}