package org.kepler42.database.operations

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import org.kepler42.database.entities.*
import org.kepler42.models.*

fun insertUsersCommunities(userCommunity: UserCommunity): UserCommunity {
    val newUserCommunity = transaction {
        addLogger(StdOutSqlLogger)
        UserCommunityEntity.new { user_id = userCommunity.userId ; community_id = userCommunity.communityId }
    }
    return newUserCommunity.toModel()
}