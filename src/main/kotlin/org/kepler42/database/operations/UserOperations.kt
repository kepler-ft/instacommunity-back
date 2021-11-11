package org.kepler42.database.operations

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import org.kepler42.database.entities.*
import org.kepler42.models.*

fun insertUsers(user: User): User {
    val newUser = transaction {
        addLogger(StdOutSqlLogger)
        UserEntity.new { name = user.name!! }
    }
    return newUser.toModel()
}
