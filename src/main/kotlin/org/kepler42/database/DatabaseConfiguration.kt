package org.kepler42.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import io.ktor.application.*

import org.kepler42.database.Greetings
// import com.sun.org.apache.xml.internal.utils.SystemIDResolver

fun Application.configureDatabase() {
    createConnection()
    createTable()
}

fun createConnection() {
    val dbServer = System.getenv("DB_SERVER")
    val dbName = System.getenv("DB_NAME")
    val dbUser = System.getenv("DB_USER")
    val dbPassword = System.getenv("DB_PASSWORD")

    Database.connect(
        "jdbc:pgsql://$dbServer:5432/$dbName",
        driver = "com.impossibl.postgres.jdbc.PGDriver",
        user = dbUser,
        password = dbPassword)
}

fun createTable() {
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Greetings)
    }
}
