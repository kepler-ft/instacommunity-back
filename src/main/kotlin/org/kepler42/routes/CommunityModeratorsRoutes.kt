package org.kepler42.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.controllers.CommunityRepository
import org.kepler42.errors.OperationNotPermittedException
import org.kepler42.errors.ResourceNotFoundException
import org.kepler42.models.User
import org.kepler42.utils.TokenValidator
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject

fun Route.moderatorsRoutes() {
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
                val communityId = call.parameters["communityId"]
                    ?: throw ResourceNotFoundException()
                val community = communityRepository.fetchCommunity(communityId.toInt())
                    ?: throw ResourceNotFoundException("Community not found")
                if (community.admin != requesterId)
                    throw OperationNotPermittedException("Can't change moderators if not admin")
                val result = communityRepository.insertModerator(communityId.toInt(), user)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }
    }
}
