package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.controllers.*
import org.kepler42.database.operations.*
import org.kepler42.errors.AlreadyRelatedException
import org.kepler42.models.*
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject

fun Route.communityRoute() {
    val communityController: CommunityController by inject()

    route("/communities") {
        get("{id}") {
            val communityId = call.parameters["id"]
            try{
                val community = communityController.getById(communityId!!.toInt())
                call.respond(community)
            } catch(e: Exception) {
                call.respond(getHttpCode(e))
            }
        }

        get {
            // val communityNameToFind =
            //         call.request.queryParameters["name"]
            //                 ?: return@get call.respond(HttpStatusCode.BadRequest)
            // val communities: List<Community>? = fetchCommunitiesByName(communityNameToFind)
            // call.respond(communities ?: emptyList())
            val communityNameToFind =  call.request.queryParameters["name"]
            val communities = if (communityNameToFind.isNullOrEmpty())
                communityController.getAll()
            else
                communityController.getByName(communityNameToFind)
            call.respond(communities ?: emptyList())
        }

        post("{id}/followers") {
            try {
                val user = call.receive<User>()
                val communityId = call.parameters["id"]

                val response = communityController.addFollower(user.id, communityId!!.toInt())
                call.respond(response)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
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
            val updatedCommunity =
                    communityController.updateCommunityByCommunityId(communityId, community)

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
                communityController.addFollower(community.creator!!.toInt(), community.id!!.toInt())
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
