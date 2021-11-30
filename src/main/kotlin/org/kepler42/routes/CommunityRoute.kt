package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.database.operations.*
import org.kepler42.models.*
import org.kepler42.controllers.*
import org.koin.ktor.ext.inject

fun Route.communityRoute() {
    val communityController: CommunityController by inject()

    route("/communities") {

        get("{id}") {
            val communityId = call.parameters["id"]
            val community = communityController.getById(communityId!!.toInt())
            if (community == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(community)
            }
        }

        get {
            // val communityNameToFind =
            //         call.request.queryParameters["name"]
            //                 ?: return@get call.respond(HttpStatusCode.BadRequest)
            // val communities: List<Community>? = fetchCommunitiesByName(communityNameToFind)
            // call.respond(communities ?: emptyList())
            val communityNameToFind =  call.request.queryParameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val communities: List<Community>? = communityController.getByName(communityNameToFind)
            call.respond(communities ?: emptyList())
        }

        post("{id}/followers") {
            try {
                val user = call.receive<User>()
                val communityId = call.parameters["id"]

                if (!checkAlreadyFollows(user.id, communityId!!.toInt())) {
                    val response: UserCommunity =
                            insertFollower(
                                    UserCommunity(
                                            userId = user.id,
                                            communityId = communityId.toInt()
                                    )
                            )
                    call.respond(response)
                } else {
                    println("já existe")
                    call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "User already follows this community")
                    )
                }
            } catch (e: ExposedSQLException) {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Something has gone pretty bad")
                )
            }
        }

        delete("{id}/followers") {
            try {
                val user = call.receive<User>()
                val communityId = call.parameters["id"]
                val response: UserCommunity =
                        deleteFollower(
                                UserCommunity(userId = user.id, communityId = communityId!!.toInt())
                        )
                call.respond(response)
                println("$response")
            } catch (e: ExposedSQLException) {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Couldn`t delete user from community")
                )
            }
        }

        get("{id}/followers") {
            /*val communityId = call.parameters["id"]
            val followers = fetchFollowers(communityId!!.toInt())
            call.respond(followers ?: emptyList())*/
            val communityId = call.parameters["id"]
            val followers = communityController.getFollowersByCommunityId(communityId!!.toInt())
            call.respond(followers ?: emptyList())
        }

        patch("{id}") {
            /*val community = call.receive<Community>()
            val communityId = call.parameters["id"]!!.toInt()
            val updatedCommunity = updateCommunity(communityId, community)

            if (updatedCommunity == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                call.respond(updatedCommunity)
            }*/
            val community = call.receive<Community>()
            val communityId = call.parameters["id"]!!.toInt()
            val updatedCommunity = communityController.updateCommunitybyCommunityId(communityId, community)

            if (updatedCommunity == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                call.respond(updatedCommunity)
            }
        }

        post {
            val community = call.receive<Community>()
            val dto = communityController.handleCommunityPost(community)

            if (dto.error == null) {
                call.respond(dto.community!!)
            } else {
                call.respond(
                    HttpStatusCode.fromValue(dto.error.code),
                    dto.error
                )
            }
        }
    }
}
