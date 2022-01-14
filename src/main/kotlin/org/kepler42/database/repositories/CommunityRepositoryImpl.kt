package org.kepler42.database.repositories

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.kepler42.controllers.CommunityRepository
import org.jetbrains.exposed.dao.id.EntityID

import org.kepler42.models.*
import org.kepler42.database.entities.*

class ILikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")

infix fun <T : String?> ExpressionWithColumnType<T>.ilike(pattern: String): Op<Boolean> =
        ILikeOp(this, QueryParameter(pattern, columnType))

class CommunityRepositoryImpl: CommunityRepository {
    override fun fetchCommunity(id: Int): Community? {
        val community = transaction {
            addLogger(StdOutSqlLogger)
            CommunityEntity.findById(id)?.toModel()
        }
        return community
    }

    override fun fetchCommunitiesByName(name: String): List<Community>? {
        val communities = transaction {
            addLogger(StdOutSqlLogger)
            CommunityEntity
                .find { CommunitiesTable.name ilike "%$name%" }
                .orderBy(CommunitiesTable.name.lowerCase() to SortOrder.ASC)
                .map { it.toModel() }
        }
        return communities
    }

    override fun fetchAllCommunities(): List<CommunityEntity>? {
        val communities = transaction {
            addLogger(StdOutSqlLogger)
            CommunityEntity.all().orderBy(CommunitiesTable.name.lowerCase() to SortOrder.ASC).toList()
        }
        return communities
    }

    override fun fetchCommunitiesFollowedByUser(userId: String): List<Community>? {
        val communities = transaction {
            addLogger(StdOutSqlLogger)
            UserEntity.findById(userId)?.communities?.orderBy(CommunitiesTable.name.lowerCase() to SortOrder.ASC)?.map { it.toModel() }
        }
        return communities
    }

    override fun insertCommunity(community: Community): Community {
        val newCommunity = transaction {
            addLogger(StdOutSqlLogger)
            CommunityEntity.new {
                name = community.name!!
                description = community.description!!
                contact = community.contact!!
                contact2 = community.contact2
                contact3 = community.contact3
                creator = EntityID(community.creator!!, UsersTable)
            }
        }
        return newCommunity.toModel()
    }

    override fun insertFollower(userCommunity: UserCommunity): UserCommunity {
        val newUserCommunity = transaction {
            addLogger(StdOutSqlLogger)
            UserCommunityEntity.new {
                user_id = EntityID(userCommunity.userId, UsersTable)
                community_id = EntityID(userCommunity.communityId, CommunitiesTable)
            }
        }
        return newUserCommunity.toModel()
    }

    override fun alreadyExists(communityName: String): Boolean {
        return transaction {
            addLogger(StdOutSqlLogger)
            val communityExists =
                CommunityEntity.find {
                    (CommunitiesTable.name ilike communityName)
                }
            communityExists.any()
        }
    }

    override fun checkAlreadyFollows(userId: String, communityId: Int): Boolean {
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

    override fun fetchFollowers(id: Int): List<User>? {
        val followers = transaction {
            addLogger(StdOutSqlLogger)
            CommunityEntity.findById(id)?.followers?.map { it.toModel() }
        }
        return followers
    }

    override fun deleteFollower(communityId: Int, userId: String) {
        transaction {
            addLogger(StdOutSqlLogger)
            UsersCommunities.deleteWhere { (UsersCommunities.user_id eq userId) and
                    (UsersCommunities.community_id eq communityId)
            }
        }
    }

    override fun updateCommunity(id: Int, community: Community): Community? {
        return transaction {
            addLogger(StdOutSqlLogger)
            val oldCommunity = CommunityEntity.findById(id)
            community.name?.let { oldCommunity?.name = community.name }
            community.description?.let { oldCommunity?.description = community.description }
            community.contact?.let { oldCommunity?.contact = community.contact }
            community.contact2?.let { oldCommunity?.contact2 = community.contact2 }
            community.contact3?.let { oldCommunity?.contact3 = community.contact3 }
            oldCommunity?.toModel()
        }
    }
}
