package org.kepler42.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import io.ktor.application.*

import org.kepler42.database.Greetings

fun Application.configureDatabase() {
    createConnection()
    createTable()
}

fun createConnection() {
    Database.connect(
        "jdbc:pgsql://localhost:5432/postgres",
        driver = "com.impossibl.postgres.jdbc.PGDriver",
        user = "postgres",
        password = "postgres")
}

fun createTable() {
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Greetings)
    }
}
