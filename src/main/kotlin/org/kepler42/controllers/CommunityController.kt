package org.kepler42.controllers

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.database.operations.*
import org.kepler42.models.*
import io.ktor.features.BadRequestException
import org.kepler42.database.entities.CommunityEntity
import org.kepler42.errors.BadRequestError
import org.kepler42.errors.Error
import org.kepler42.errors.InternalServerError

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
	fun nameIsValid(name: String?) =
        if (name == null) false
        else if (name.isEmpty()) false
        else name.length < 200
    fun getById(id: Int) = communityRepository.fetchCommunity(id)
    fun getByName(communityNameToFind: String) = communityRepository.fetchCommunitiesByName(communityNameToFind)
    fun getAll(): List<Community>? = communityRepository.fetchAllCommunities()?.map{ it.toModel() }
    // precisa fazer as coisas que a rota na rua faz

    fun insertFollowerByModel(userCommunity: UserCommunity) = communityRepository.insertFollower(userCommunity)
    fun getFollowersByCommunityId(communityId: Int) = communityRepository.fetchFollowers(communityId)
    fun updateCommunitybyCommunityId(id: Int, community: Community) = communityRepository.updateCommunity(id, community)
    fun insertCommunityByModel(community: Community) = communityRepository.insertCommunity(community)

	fun handleCommunityPost(community: Community): CommunityDTO {
		var error: Error?
		var communityRet: Community?

		if (!nameIsValid(community.name)) {
            communityRet = null
            error = BadRequestError("Name is invalid")
        } else {
            try {
                if (checkAlreadyExists(community.name!!)) {
                    communityRet = null
                    error = BadRequestError("A community with this name already exists")
                } else {
                    communityRet = insertCommunities(community)
                    error = null
                }
            } catch (e: ExposedSQLException) {
                communityRet = null
                error = InternalServerError()
            }
        }
        return CommunityDTO(communityRet, error)
	}
}
