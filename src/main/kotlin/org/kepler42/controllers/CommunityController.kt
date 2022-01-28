package org.kepler42.controllers

import org.kepler42.models.*
import org.kepler42.database.entities.CommunityEntity
import org.kepler42.errors.*

interface CommunityRepository {
    fun search(name: String? = null,  tags: List<Int>? = null): List<Community>?
    fun fetchCommunity(id: Int): Community?
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
    fun insertModerator(communityId: Int, moderator: User): User
}

class CommunityController(private val communityRepository: CommunityRepository) {
    private fun nameIsValid(name: String) =
            if (name.isEmpty()) false else name.length < 200

    fun getById(id: Int):Community {
        return communityRepository.fetchCommunity(id)
            ?: throw ResourceNotFoundException("Community Not Found")
    }

    fun search(name: String?, tagId: List<Int>?): List<Community> {
        return communityRepository.search(name, tagId) ?: emptyList()
    }

    fun getAll(desiredPage: Long): List<Community> {
        val page = if (desiredPage > 0) desiredPage
                   else 1
        return communityRepository.fetchAllCommunities(page)
    }

    fun addFollower(userId: String, communityId: Int) {
        val alreadyFollows = communityRepository.checkAlreadyFollows(userId, communityId)
        if (alreadyFollows) throw AlreadyRelatedException("This user already follows this community")

        communityRepository.insertFollower(userId, communityId)
    }

    fun removeFollower(communityId: Int, userId: String) {
        communityRepository.deleteFollower(communityId, userId)
    }

    fun getCommunityFollowers(communityId: Int) =
            communityRepository.fetchFollowers(communityId)

    fun updateCommunity(userId: String, id: Int, community: Community): Community {
        val comm = communityRepository.fetchCommunity(id) ?: throw ResourceNotFoundException()
        if (comm.admin != userId)
            throw UnauthorizedException()
        return communityRepository.updateCommunity(id, community) ?: throw ResourceNotFoundException("Community not found")
    }

    fun createCommunity(community: Community): Community {
        if (community.name == null || !nameIsValid(community.name))
            throw InvalidNameException()

        if (community.contacts.isEmpty())
            throw InvalidBodyException("A community needs at least one contact")

        if (community.contacts.size > 3)
            throw InvalidBodyException("A community can't have more than 3 contacts")

        if (communityRepository.alreadyExists(community.name))
            throw AlreadyExistsException("A community with this name already exists")

        val createdCommunity = communityRepository.insertCommunity(community)
        if (community.admin != null) {
            communityRepository.insertFollower(community.admin, createdCommunity.id)
        }

        return createdCommunity
    }
}
