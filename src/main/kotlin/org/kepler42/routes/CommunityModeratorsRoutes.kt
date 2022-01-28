package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.controllers.CommunityRepository
import org.kepler42.errors.InvalidBodyException
import org.kepler42.errors.ResourceNotFoundException
import org.kepler42.models.User
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject

fun Route.moderatorsRoutes() {
    val communityRepository by inject<CommunityRepository>()

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
                val user = call.receive<User>()
                val communityId = call.parameters["communityId"]
                    ?: throw ResourceNotFoundException()
                val result = communityRepository.insertModerator(communityId.toInt(), user)
                call.respond(result)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }
    }
}
