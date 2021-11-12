package org.kepler42.database.operations

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import org.kepler42.database.entities.*
import org.kepler42.models.*

fun insertCommunities(user: Community): Community {
    val newCommunity = transaction {
        addLogger(StdOutSqlLogger)
        CommunityEntity.new { name = user.name!! }
    }
    return newCommunity.toModel()
}

