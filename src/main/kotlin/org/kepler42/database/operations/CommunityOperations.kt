package org.kepler42.database.operations

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.dao.id.EntityID

import org.kepler42.database.entities.*
import org.kepler42.models.*

fun insertCommunities(user: Community): Community {
    val newCommunity = transaction {
        addLogger(StdOutSqlLogger)
        CommunityEntity.new { name = user.name }
    }
    return newCommunity.toModel()
}

fun fetchCommunity(id: Int): Community? {
    val community = transaction {
        addLogger(StdOutSqlLogger)
        CommunityEntity.findById(id)?.toModel()
    }
    return community
}

fun insertFollower(userCommunity: UserCommunity): UserCommunity {
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
        addLogger(StdOutSqlLogger)
        val follows = UserCommunityEntity.find { (UsersCommunities.user_id eq userId) and (UsersCommunities.community_id eq communityId) }
        follows.any()
    }
}

fun fetchFollowers(id: Int): List<User>? {
    val followers = transaction {
        addLogger(StdOutSqlLogger)
        CommunityEntity.findById(id)?.followers?.map { it.toModel() }
    }
    return followers
}
