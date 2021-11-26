package org.kepler42.database.operations

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kepler42.database.entities.*
import org.kepler42.models.*

class ILikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")

infix fun <T : String?> ExpressionWithColumnType<T>.ilike(pattern: String): Op<Boolean> =
        ILikeOp(this, QueryParameter(pattern, columnType))

fun insertCommunities(community: Community): Community {
    val newCommunity = transaction {
        addLogger(StdOutSqlLogger)
        CommunityEntity.new {
            name = community.name!!
            description = community.description!!
            contact = community.contact
        }
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
            user_id = EntityID<Int>(userCommunity.userId, Users)
            community_id = EntityID<Int>(userCommunity.communityId, Communities)
        }
    }
    return newUserCommunity.toModel()
}

fun deleteFollower(userCommunity: UserCommunity): UserCommunity {
    transaction {
        addLogger(StdOutSqlLogger)
        val relationToDelete = UserCommunityEntity.find {
            (UsersCommunities.user_id eq userCommunity.userId) and
                    (UsersCommunities.community_id eq userCommunity.communityId)
        }.toList().first()
        relationToDelete.delete()
    }
    return userCommunity
}

fun checkAlreadyFollows(userId: Int, communityId: Int): Boolean {
    return transaction {
        addLogger(StdOutSqlLogger)
        val follows =
                UserCommunityEntity.find {
                    (UsersCommunities.user_id eq userId) and
                            (UsersCommunities.community_id eq communityId)
                }
        follows.any()
    }
}

fun checkAlreadyExists(communityName: String): Boolean {
    return transaction {
        addLogger(StdOutSqlLogger)
        val communityExists =
                CommunityEntity.find {
                    (Communities.name ilike communityName)
                }
        communityExists.any()
    }
}

fun fetchFollowers(id: Int): List<User>? {
    val followers = transaction {
        addLogger(StdOutSqlLogger)
        CommunityEntity.findById(id)?.followers?.map { it.toModel() }
    }
    return followers
}

fun updateCommunity(id: Int, community: Community): Community? {
    return transaction {
        addLogger(StdOutSqlLogger)
        val oldCommunity = CommunityEntity.findById(id)
        community.name?.let { oldCommunity?.name = community.name }
        community.description?.let { oldCommunity?.description = community.description }
        community.contact?.let { oldCommunity?.contact = community.contact }
        oldCommunity?.toModel()
    }
}

fun fetchCommunitiesByName(name: String): List<Community>? {
    val communities = transaction {
        addLogger(StdOutSqlLogger)
        CommunityEntity.find { Communities.name ilike "%$name%" }.map { it.toModel() }
    }
    return communities
}
