package org.kepler42.controllers

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.database.operations.*
import org.kepler42.models.*
import org.kepler42.database.entities.CommunityEntity
import org.kepler42.errors.*

interface CommunityRepository {
    fun fetchCommunity(id: Int): Community?
    fun fetchCommunitiesByName(name: String): List<Community>?
    fun insertCommunity(community: Community): Community
    fun insertFollower(userCommunity: UserCommunity): UserCommunity
    fun checkAlreadyExists(communityName: String): Boolean
    fun fetchFollowers(id: Int): List<User>?
    fun updateCommunity(id: Int, community: Community): Community?
    fun fetchAllCommunities(): List<CommunityEntity>?
}

data class CommunityDTO(
    val community: Community?,
    val error: Error?,
)

class CommunityController(private val communityRepository: CommunityRepository) {
    private fun nameIsValid(name: String?) =
            if (name == null) false else if (name.isEmpty()) false else name.length < 200

    fun getById(id: Int):Community {
        return communityRepository.fetchCommunity(id)
            ?: throw ResourceNotFoundException("Community Not Found")
    }

    fun getByName(communityNameToFind: String) = communityRepository.fetchCommunitiesByName(communityNameToFind)

    fun getAll(): List<Community>? = communityRepository.fetchAllCommunities()?.map{ it.toModel() }

    fun addFollower(userId: String, communityId: Int) {
        val alreadyFollows = checkAlreadyFollows(userId, communityId)
        if (alreadyFollows) throw AlreadyRelatedException("This user already follows this community")

        val relation = UserCommunity(userId = userId, communityId = communityId)
        communityRepository.insertFollower(relation)
    }
    fun getFollowersByCommunityId(communityId: Int) =
            communityRepository.fetchFollowers(communityId)

    fun updateCommunityByCommunityId(id: Int, community: Community) =
            communityRepository.updateCommunity(id, community)

    fun insertCommunityByModel(community: Community) =
            communityRepository.insertCommunity(community)

    fun handleCommunityPost(community: Community): CommunityDTO {
        var error: Error? = null
        var communityRet: Community? = null

        if (!nameIsValid(community.name)) {
            error = BadRequestError("Name is invalid")
        } else {
            try {
                if (checkAlreadyExists(community.name!!)) {
                    error = BadRequestError("A community with this name already exists")
                } else {
                    communityRet = insertCommunities(community)
                }
            } catch (e: ExposedSQLException) {
                error = InternalServerError()
            }
        }
        return CommunityDTO(communityRet, error)
    }
}
