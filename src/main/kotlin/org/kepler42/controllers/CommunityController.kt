package org.kepler42.controllers

import org.kepler42.models.*
import org.kepler42.database.entities.CommunityEntity
import org.kepler42.errors.*

interface CommunityRepository {
    fun fetchCommunity(id: Int): Community?
    fun fetchCommunitiesByName(name: String): List<Community>?
    fun insertCommunity(community: Community): Community
    fun insertFollower(userCommunity: UserCommunity): UserCommunity
    fun deleteFollower(communityId: Int, userId: String)
    fun alreadyExists(communityName: String): Boolean
    fun fetchFollowers(id: Int): List<User>?
    fun updateCommunity(id: Int, community: Community): Community?
    fun fetchAllCommunities(): List<CommunityEntity>?
    fun fetchCommunitiesFollowedByUser(userId: String): List<Community>?
    fun checkAlreadyFollows(userId: String, communityId: Int): Boolean
}

class CommunityController(private val communityRepository: CommunityRepository) {
    private fun nameIsValid(name: String) =
            if (name.isEmpty()) false else name.length < 200

    fun getById(id: Int):Community {
        return communityRepository.fetchCommunity(id)
            ?: throw ResourceNotFoundException("Community Not Found")
    }

    fun searchByName(communityNameToFind: String): List<Community> {
        return communityRepository.fetchCommunitiesByName(communityNameToFind) ?: emptyList()
    }

    fun getAll(): List<Community> {
        return communityRepository.fetchAllCommunities()?.map{ it.toModel() } ?: emptyList()
    }

    fun addFollower(userId: String, communityId: Int) {
        val alreadyFollows = communityRepository.checkAlreadyFollows(userId, communityId)
        if (alreadyFollows) throw AlreadyRelatedException("This user already follows this community")

        val relation = UserCommunity(userId = userId, communityId = communityId)
        communityRepository.insertFollower(relation)
    }

    fun removeFollower(communityId: Int, userId: String) {
        communityRepository.deleteFollower(communityId, userId)
    }

    fun getCommunityFollowers(communityId: Int) =
            communityRepository.fetchFollowers(communityId)

    fun updateCommunity(userId: String, id: Int, community: Community) {
        val comm = communityRepository.fetchCommunity(id) ?: throw ResourceNotFoundException()
        if (comm.admin != userId)
            throw UnauthorizedException()
        communityRepository.updateCommunity(id, community)
    }

    fun createCommunity(community: Community): Community {
        if (community.name == null || !nameIsValid(community.name))
            throw InvalidNameException()

        if (communityRepository.alreadyExists(community.name))
            throw AlreadyExistsException("A community with this name already exists")

        val createdCommunity = communityRepository.insertCommunity(community)
        if (community.admin != null) {
            val relation = UserCommunity(userId = community.admin, communityId = createdCommunity.id)
            communityRepository.insertFollower(relation)
        }

        return createdCommunity
    }
}
