package com.jobis.extension

import com.jobis.config.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class TestDatabaseExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        System.setProperty("profile", "test")
        DatabaseConfig.connect()
    }
}

fun withTestTransaction(vararg tables: Table, block: () -> Unit) {
    transaction {
        SchemaUtils.createMissingTablesAndColumns(*tables)
        block()
    }
}