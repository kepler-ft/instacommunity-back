package org.kepler42.controllers

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.database.operations.*
import org.kepler42.errors.BadRequestError
import org.kepler42.errors.Error
import org.kepler42.errors.InternalServerError
import org.kepler42.models.*

interface CommunityRepository {
    fun fetchCommunity(id: Int): Community?
    fun fetchCommunitiesByName(name: String): List<Community>?
    fun insertCommunity(community: Community): Community
    fun insertFollower(userCommunity: UserCommunity): UserCommunity
    fun checkAlreadyExists(communityName: String): Boolean
    fun fetchFollowers(id: Int): List<User>?
    fun updateCommunity(id: Int, community: Community): Community?
}

data class CommunityDTO(
    val community: Community?,
    val error: Error?,
)

class CommunityController(private val communityRepository: CommunityRepository) {
    private fun nameIsValid(name: String?) =
            if (name == null) false else if (name.isEmpty()) false else name.length < 200
    fun getById(id: Int) = communityRepository.fetchCommunity(id)
    fun getByName(communityNameToFind: String) =
            communityRepository.fetchCommunitiesByName(communityNameToFind)


    fun insertFollowerByModel(userCommunity: UserCommunity) =
            communityRepository.insertFollower(userCommunity)
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
