package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.utils.io.*
import org.jetbrains.exposed.sql.idParam
import org.kepler42.controllers.*
import org.kepler42.database.repositories.CommunityRepositoryImpl
import org.kepler42.errors.UnauthorizedException
import org.kepler42.models.*
import org.kepler42.utils.TokenValidator
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject

fun Route.communityRoute() {
    val communityController: CommunityController by inject()
    val validator: TokenValidator by inject()

    route("/communities") {
        get {
            val communityNameToFind =  call.request.queryParameters["name"]
            val desiredPage = call.request.queryParameters["page"] ?: "1"
            val tagString = call.request.queryParameters["tags"] // "4,2,3"
            val communitySlug = call.parameters["slug"]
            try {
                val communities = if ( communityNameToFind.isNullOrEmpty() && tagString.isNullOrEmpty() && communitySlug.isNullOrEmpty())
                    communityController.getAll(desiredPage.toLong())
                else if (!communitySlug.isNullOrEmpty()){
                    communityController.getBySlug(communitySlug)
                }
                else {
                    val tagList = tagString?.split(",")?.map { it.toInt() }
                    communityController.search(communityNameToFind, tagList)
                }
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
            try {
                val communityId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "missing community id")
                val userId = validator.checkAuth(call)
                val response = communityController.addFollower(userId, communityId.toInt())
                call.respond(response)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        delete("{communityId}/followers/{followerId}") {
            try {
                val userId = validator.checkAuth(call)
                val communityId = call.parameters["communityId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "missing community id"))
                val followerId = call.parameters["followerId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing user id"))
                if (followerId != userId)
                    throw UnauthorizedException("You can't make other users do what you want")
                communityController.removeFollower(communityId.toInt(), followerId)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        get("{id}/followers") {
            val communityId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode. BadRequest, "missing community id")
            try {
                val followers = communityController.getCommunityFollowers(communityId.toInt())
                call.respond(followers ?: emptyList())
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        patch("{id}") {
            try {
                val userId = validator.checkAuth(call)
                val community = call.receive<Community>()
                val communityId = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "missing community id")
                val updatedCommunity = communityController.updateCommunity(userId, communityId.toInt(), community)
                call.respond(updatedCommunity)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        post {
            try {
                val id = validator.checkAuth(call)

                val community = call.receive<Community>()
                if (id != community.admin)
                    throw UnauthorizedException("Can't create community in the name of another user")
                val createdCommunity = communityController.createCommunity(community)
                call.respond(createdCommunity)
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        post("{communityId}/contacts") {
            try {
                val userId = validator.checkAuth(call)
                val contacts = call.receive<List<Contact>>()
                val communityId = call.parameters["communityId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "missing community id")
                val response = communityController.addContacts(contacts, communityId.toInt(), userId)
                call.respond(response)
            }
            catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        patch("{communityId}/contacts/{contactId}") {
            try {
                val userId = validator.checkAuth(call)
                val contact = call.receive<Contact>()
                val communityId = call.parameters["communityId"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "missing community id")
                val response = communityController.updateContact(contact, communityId.toInt(), userId)
                call.respond(response)
            }
            catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        delete("{communityId}/contacts/{contactId}") {
            try {
                val userId = validator.checkAuth(call)
                val contact = call.receive<Contact>()
                val communityId = call.parameters["communityId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "missing community id")
                communityController.removeContact(contact, communityId.toInt(), userId)
                call.respond(HttpStatusCode.OK)
            }
            catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }
    }
}
