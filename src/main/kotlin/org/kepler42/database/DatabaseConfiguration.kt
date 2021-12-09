package org.kepler42.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import io.ktor.application.*
import org.flywaydb.core.Flyway

fun Application.configureDatabase() {
    val dbServer = System.getenv("DB_SERVER")
    val dbName = System.getenv("DB_NAME")
    val dbUser = System.getenv("DB_USER")
    val dbPassword = System.getenv("DB_PASSWORD")

    createConnection(dbServer, dbName, dbUser, dbPassword)
    runMigrations(dbServer, dbName, dbUser, dbPassword)
}

fun createConnection(dbServer: String, dbName: String, user: String, password: String) {
    val url = "jdbc:pgsql://$dbServer:5432/$dbName"
    Database.connect(
        url = url,
        driver = "com.impossibl.postgres.jdbc.PGDriver",
        user = user,
        password = password)
}

fun runMigrations(dbServer: String, dbName: String, user: String, password: String) {
    val url = "jdbc:postgresql://$dbServer:5432/$dbName"
    val flyway = Flyway.configure().dataSource(url, user, password).load()
    flyway.migrate()
}
