package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.controllers.*
import org.kepler42.models.*
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject

fun Route.communityRoute() {
    val communityController: CommunityController by inject()

    route("/communities") {
        get {
            val communityNameToFind =  call.request.queryParameters["name"]
            try {
                val communities = if (communityNameToFind.isNullOrEmpty())
                    communityController.getAll()
                else
                    communityController.getByName(communityNameToFind)
                call.respond(communities)
            } catch (e: Exception) {
                call.respond(getHttpCode(e))
            }
        }

        get("{id}") {
            val communityId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "missing community id")
            try{
                val community = communityController.getById(communityId.toInt())
                call.respond(community)
            } catch(e: Exception) {
                call.respond(getHttpCode(e))
            }
        }

        post("{id}/followers") {
            val communityId = call.parameters["id"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "missing community id")
            try {
                val user = call.receive<User>()
                if (user.id == null)
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing user id"))

                val response = communityController.addFollower(user.id, communityId.toInt())
                call.respond(response)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        delete("{id}/followers") {
            val communityId = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "missing community id")
            try {
                val user = call.receive<User>()
                if (user.id == null)
                    return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing user id"))

                communityController.removeFollower(communityId.toInt(), user.id)
                call.respond(HttpStatusCode.OK)
            } catch (e: ExposedSQLException) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        get("{id}/followers") {
            val communityId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "missing community id")
            try {
                val followers = communityController.getCommunityFollowers(communityId.toInt())
                call.respond(followers ?: emptyList())
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        patch("{id}") {
            val community = call.receive<Community>()
            val communityId = call.parameters["id"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "missing community id")
            try {
                val updatedCommunity = communityController.updateCommunity(communityId.toInt(), community)
                call.respond(updatedCommunity)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        post {
            val community = call.receive<Community>()
            try {
                val createdCommunity = communityController.createCommunity(community)
                call.respond(createdCommunity)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }
    }
}
