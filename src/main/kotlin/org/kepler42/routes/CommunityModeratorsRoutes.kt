package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.controllers.CommunityRepository
import org.kepler42.database.repositories.UserRepository
import org.kepler42.errors.InvalidBodyException
import org.kepler42.errors.OperationNotPermittedException
import org.kepler42.errors.ResourceNotFoundException
import org.kepler42.models.User
import org.kepler42.utils.TokenValidator
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject

fun Route.moderatorsRoutes() {
    val userRepository by inject<UserRepository>()
    val communityRepository by inject<CommunityRepository>()
    val tokenValidator by inject<TokenValidator>()

    route("/communities/{communityId}/moderators") {
        get {
            try {
                val communityId = call.parameters["communityId"]
                    ?: throw ResourceNotFoundException()
                val moderators = communityRepository.fetchModerators(communityId.toInt())
                    ?: throw ResourceNotFoundException()
                call.respond(moderators)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        post {
            try {
                val requesterId = tokenValidator.checkAuth(call)
                val user = call.receive<User>()
                if (user.id == null)
                    throw InvalidBodyException("Missing user id")
                val communityId = call.parameters["communityId"]
                    ?: throw ResourceNotFoundException()
                val community = communityRepository.fetchCommunity(communityId.toInt())
                    ?: throw ResourceNotFoundException("Community not found")
                if (community.admin != requesterId)
                    throw OperationNotPermittedException("Can't change moderators if not admin")
                val moderator = userRepository.getUserById(user.id)
                    ?: throw ResourceNotFoundException("User not found")
                val result = communityRepository.insertModerator(communityId.toInt(), moderator.id!!)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        delete("{userId}") {
            try {
                val requesterId = tokenValidator.checkAuth(call)

                val communityId = call.parameters["communityId"]
                    ?: throw ResourceNotFoundException()
                val moderatorId = call.parameters["userId"]
                    ?: throw ResourceNotFoundException()
                val community = communityRepository.fetchCommunity(communityId.toInt())
                    ?: throw ResourceNotFoundException("Community not found")
                if (community.admin != requesterId)
                    throw OperationNotPermittedException("Can't change moderators if not admin")
                val moderator = userRepository.getUserById(moderatorId)
                    ?: throw ResourceNotFoundException("User not found")

                communityRepository.deleteModerator(community.id, moderator.id!!)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }
    }
}
