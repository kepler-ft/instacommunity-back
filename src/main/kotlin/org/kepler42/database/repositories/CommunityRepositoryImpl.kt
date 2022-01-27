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
    override fun search(name: String?, tags: List<Int>?): List<Community>? {
        TODO("Not yet implemented")
    }

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

    override fun fetchAllCommunities(page: Long): List<Community> {
        val pageSize = 5
        val communities = transaction {
            addLogger(StdOutSqlLogger)
            CommunityEntity
                .all()
                .orderBy(CommunitiesTable.name.lowerCase() to SortOrder.ASC)
                .limit(pageSize, pageSize * (page - 1))
                .toList().map { it.toModel() }
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
            val createdCommunity = CommunityEntity.new {
                name = community.name!!
                description = community.description!!
                admin = EntityID(community.admin!!, UsersTable)
                slug = community.slug!!
                type = community.type!!
                photo_url = community.photo_url!!
            }
            for (contact in community.contacts) {
                ContactEntity.new {
                    this.community = createdCommunity
                    this.title = contact.title!!
                    this.link = contact.link
                }
            }
            createdCommunity.toModel()
        }
        return newCommunity
    }

    override fun insertFollower(userId: String, communityId: Int) {
        transaction {
            addLogger(StdOutSqlLogger)
            UserCommunityEntity.new {
                user_id = EntityID(userId, UsersTable)
                community_id = EntityID(communityId, CommunitiesTable)
            }
        }
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
            val oldCommunity = CommunityEntity.findById(id) ?: return@transaction null
            community.name?.let { oldCommunity.name = community.name }
            community.description?.let { oldCommunity.description = community.description }
            community.admin?.let { oldCommunity.admin = EntityID(community.admin, UsersTable) }
            community.photo_url?.let { oldCommunity.photo_url = community.photo_url }
            oldCommunity.toModel()
        }
    }
}
