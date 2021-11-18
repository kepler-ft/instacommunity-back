package org.kepler42.routes

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*

import org.kepler42.models.*
import org.kepler42.database.operations.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

private fun invalidName(name: String?) =
    if (name == null) true
    else if (name.length < 1) true
    else if (name.length > 200) true
    else false

fun Route.communityRoute() {
        route("/communities") {
            get ("{id}") {
                val communityId = call.parameters["id"]
                val community: Community? = fetchCommunity(communityId!!.toInt())
                if (community == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(community)
                }
            }

            post ("{id}/followers") {
                try {
                    val user = call.receive<User>()
                    val communityId = call.parameters["id"]

                    if (!checkAlreadyFollows(user.id, communityId!!.toInt())) {
                        val response: UserCommunity = insertFollower(UserCommunity(
                            userId = user.id,
                            communityId = communityId.toInt()))
                        call.respond(response)
                    } else {
                        println ("já existe")
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "User already follows this community"))
                    }
                } catch (e: ExposedSQLException) {
                    call.respond(HttpStatusCode.InternalServerError,
                    mapOf("error" to "Something has gone pretty bad"))
                }
            }

            get ("{id}/followers") {
                val communityId = call.parameters["id"]
                val followers = fetchFollowers(communityId!!.toInt())
                call.respond(followers ?: emptyList())
            }

            patch ("{id}") {
                val community = call.receive<Community>()
                val communityId = call.parameters["id"]!!.toInt()
                val updatedCommunity =  updateCommunity(communityId, community)

                if (updatedCommunity == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    call.respond(updatedCommunity)
                }
            }

            get {
                val communityNameToFind = call.request.queryParameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val communities: List<Community>? = fetchCommunitiesByName(communityNameToFind)
                call.respond(communities ?: emptyList())
            }

            post {
                val community = call.receive<Community>()
                if (invalidName(community.name)) {
                    return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid name"))
                }
                try {
                    if (checkAlreadyExists(community.name!!)) {
                        return@post call.respond(HttpStatusCode.BadRequest,
                        mapOf("error" to "Community already exists"))
                    }
                    else call.respond(insertCommunities(community))
                } catch (e: ExposedSQLException) {
                    call.respond(mapOf("error" to "Something has gone pretty bad"))
                }
            }
    }
}
