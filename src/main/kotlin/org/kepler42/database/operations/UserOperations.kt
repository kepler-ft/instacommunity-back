package org.kepler42.database.operations

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import org.kepler42.database.entities.*
import org.kepler42.models.*

fun fetchCommunitiesByUserId(userId: Int ): List<Community>? {
    val communities = transaction {
        addLogger(StdOutSqlLogger)
        UserEntity.findById(userId)?.communities?.orderBy(CommunitiesTable.name.lowerCase() to SortOrder.ASC)?.map { it.toModel() }
    }
    return communities
}
