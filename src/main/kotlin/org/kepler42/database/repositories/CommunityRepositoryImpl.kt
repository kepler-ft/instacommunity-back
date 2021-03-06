package org.kepler42.database.repositories

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kepler42.database.entities.*
import org.kepler42.database.repositories.utils.insensitiveLike
import org.kepler42.errors.InvalidBodyException
import org.kepler42.models.Community
import org.kepler42.models.Contact
import org.kepler42.models.User

interface CommunityRepository {
    fun search(name: String? = null,  tags: List<Int>? = null): List<Community>?
    fun fetchCommunity(id: Int): Community?
    fun fetchCommunityBySlug(slug: String): Community?
    fun fetchCommunitiesByName(name: String): List<Community>?
    fun insertCommunity(community: Community): Community
    fun insertFollower(userId: String, communityId: Int)
    fun deleteFollower(communityId: Int, userId: String)
    fun alreadyExists(communityName: String): Boolean
    fun fetchFollowers(id: Int): List<User>?
    fun updateCommunity(id: Int, community: Community): Community?
    fun fetchAllCommunities(page: Long): List<Community>
    fun fetchCommunitiesFollowedByUser(userId: String): List<Community>?
    fun checkAlreadyFollows(userId: String, communityId: Int): Boolean
    fun fetchModerators(communityId: Int): List<User>?
    fun insertModerator(communityId: Int, userId: String)
    fun deleteModerator(communityId: Int, moderatorId: String)
    fun insertContacts(contacts: List<Contact>, communityId: Int): List<Contact>
    fun updateContact(contact: Contact, communityId: Int): Contact?
    fun deleteContact(contact: Contact, communityId: Int): Contact?
}

class CommunityRepositoryImpl: CommunityRepository {
    override fun search(name: String?, tags: List<Int>?): List<Community>? {
        return transaction {
            addLogger(StdOutSqlLogger)
            val fields = CommunitiesTable.leftJoin(CommunitiesTagsTable)
                .slice(CommunitiesTable.columns)
            val query = if (name == null && tags == null)
                fields.selectAll().orderBy(CommunitiesTable.name.lowerCase() to SortOrder.ASC)
            else
                fields.select {
                    if (tags == null)
                        CommunitiesTable.name insensitiveLike "%$name%"
                    else if (name == null)
                        TagsTable.id inList tags
                    else
                        CommunitiesTable.name insensitiveLike "%$name%" and (TagsTable.id inList tags)
                }
                    .orderBy(CommunitiesTable.name.lowerCase() to SortOrder.ASC)
            CommunityEntity.wrapRows(query).map { it.toModel() }
        }
    }

    override fun fetchCommunity(id: Int): Community? {
        val community = transaction {
            addLogger(StdOutSqlLogger)
            CommunityEntity.findById(id)?.toModel()
        }
        return community
    }

    override fun fetchCommunityBySlug(slug: String): Community? {
        val community = transaction {
            addLogger(StdOutSqlLogger)
            CommunityEntity
                .find { CommunitiesTable.slug eq slug }
                .map { it.toModel() }
                .firstOrNull()
            }
            return community
        }

    override fun fetchCommunitiesByName(name: String): List<Community>? {
        val communities = transaction {
            addLogger(StdOutSqlLogger)
            CommunityEntity
                .find { CommunitiesTable.name insensitiveLike "%$name%" }
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
                .orderBy(CommunitiesTable.id to SortOrder.DESC)
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

            val desiredSlug = community.slug ?: throw InvalidBodyException("Missing slug")
            val existingSlug = CommunityEntity.find { CommunitiesTable.slug eq desiredSlug }.map { it.slug }.firstOrNull()
            val slugToInsert = if (existingSlug == null)
                desiredSlug
            else
                ""

            val createdCommunity = CommunityEntity.new {
                name = community.name!!
                description = community.description!!
                admin = EntityID(community.admin!!, UsersTable)
                slug = slugToInsert
                type = community.type!!
                photo_url = community.photoURL!!
            }
            if (slugToInsert == "") {
                createdCommunity.slug = desiredSlug + createdCommunity.id
            }
            for (contact in community.contacts) {
                ContactEntity.new {
                    this.community = createdCommunity.id
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
                    (CommunitiesTable.name insensitiveLike communityName)
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

    override fun fetchModerators(communityId: Int): List<User>? {
        return transaction {
            CommunityEntity.findById(communityId)?.moderators?.map { it.toModel() }
        }
    }

    override fun insertModerator(communityId: Int, userId: String) {
        return transaction {
            CommunitiesModeratorsTable.insert {
                it[user_id] = userId
                it[community_id] = communityId
            }
        }
    }

    override fun deleteModerator(communityId: Int, moderatorId: String) {
        TODO("Not yet implemented")
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
            community.photoURL?.let { oldCommunity.photo_url = community.photoURL }
            oldCommunity.toModel()
        }
    }

    override fun insertContacts(contacts: List<Contact>, communityId: Int): List<Contact> {
       val newContacts = transaction {
           addLogger(StdOutSqlLogger)
           contacts.map { contact ->
               ContactEntity.new {
                   this.title = contact.title!!
                   this.link = contact.link
                   this.community = EntityID(communityId, CommunitiesTable)
               }.toModel()
           }
       }
       return newContacts
    }

    override fun updateContact(contact: Contact, communityId: Int): Contact? {
        return transaction {
            addLogger(StdOutSqlLogger)
            val oldContact = ContactEntity.findById(contact.id!!) ?: return@transaction null
            contact.title?.let { oldContact.title = contact.title }
            contact.link?.let { oldContact.link = contact.link }
            oldContact.toModel()
        }
    }

    override fun deleteContact(contact: Contact, communityId: Int): Contact? {
        return transaction {
            addLogger(StdOutSqlLogger)
            val oldContact = ContactEntity.findById(contact.id!!) ?: return@transaction null
            oldContact.delete()
            oldContact.toModel()
        }
    }
}
