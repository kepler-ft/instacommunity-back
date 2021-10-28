package org.kepler42.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import org.kepler42.database.GreetingEntity
import org.kepler42.models.Greeting 

fun fetchGreeting(id: Long): Greeting? {
    val greeting = transaction {
        addLogger(StdOutSqlLogger)
        GreetingEntity.findById(id.toInt())
    }

    return greeting?.toModel()
}