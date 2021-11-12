package org.kepler42.database.operations

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.dao.id.EntityID

import org.kepler42.database.entities.*
import org.kepler42.models.*

fun insertUsersCommunities(userCommunity: UserCommunity): UserCommunity {
    val newUserCommunity = transaction {
        addLogger(StdOutSqlLogger)
        UserCommunityEntity.new {
            user_id = EntityID<Int>(userCommunity.userId, Users);
            community_id = EntityID<Int>(userCommunity.communityId, Communities) }
    }
    return newUserCommunity.toModel()
}

fun checkAlreadyFollows(userId: Int, communityId: Int) : Boolean {
    return transaction {
        val follows = UserCommunityEntity.find { (UsersCommunities.user_id eq userId) and (UsersCommunities.community_id eq communityId) }
        follows.any()
    }
}