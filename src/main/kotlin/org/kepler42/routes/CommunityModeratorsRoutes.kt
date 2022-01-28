package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.controllers.CommunityRepository
import org.kepler42.errors.ResourceNotFoundException
import org.koin.ktor.ext.inject

fun Route.moderatorsRoutes() {
    val communityRepository by inject<CommunityRepository>()

    route("/communities/{communityId}/moderators") {
        get {
            val communityId = call.parameters["communityId"] ?: throw ResourceNotFoundException()
            val moderators = communityRepository.fetchModerators(communityId.toInt()) ?: throw ResourceNotFoundException()
            call.respond(moderators)
        }
    }
}
