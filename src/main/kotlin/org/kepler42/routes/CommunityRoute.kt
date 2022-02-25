package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.database.repositories.CommunityRepository
import org.kepler42.errors.*
import org.kepler42.models.Community
import org.kepler42.models.Contact
import org.kepler42.utils.TokenValidator
import org.kepler42.utils.getHttpCode
import org.kepler42.utils.sendErrorResponse
import org.koin.ktor.ext.inject

private fun nameIsValid(name: String) =
    if (name.isEmpty()) false else name.length < 200

fun Route.communityRoute() {
    val communityRepository: CommunityRepository by inject()
    val validator: TokenValidator by inject()

    route("/communities") {
        get {
            val communityNameToFind =  call.request.queryParameters["name"]
            val desiredPage = call.request.queryParameters["page"] ?: "1"
            val tagString = call.request.queryParameters["tags"] // "4,2,3"
            val communitySlug = call.parameters["slug"]
            try {
                if ( communityNameToFind.isNullOrEmpty() && tagString.isNullOrEmpty() && communitySlug.isNullOrEmpty()) {
                    val page = if (desiredPage.toInt() > 0) desiredPage.toLong() else 1
                    val communities = communityRepository.fetchAllCommunities(page)
                    call.respond(communities)

                } else if (!communitySlug.isNullOrEmpty()){
                    val community = communityRepository.fetchCommunityBySlug(communitySlug)
                        ?: throw ResourceNotFoundException("There's no community with this slug")
                    call.respond(community)

                } else {
                    val tagList = tagString?.split(",")?.map { it.toInt() }
                    val communities = communityRepository.search(communityNameToFind, tagList)
                        ?: emptyList()
                    call.respond(communities)
                }
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        get("{id}") {
            val communityId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "missing community id")
            try{
                val community = communityRepository.fetchCommunity(communityId.toInt())
                    ?: throw ResourceNotFoundException("Community Not Found")
                call.respond(community)
            } catch(e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        post("{id}/followers") {
            try {
                val communityIdStr = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "missing community id")
                val userId = validator.checkAuth(call)
                val communityId = communityIdStr.toInt()

                val alreadyFollows = communityRepository.checkAlreadyFollows(userId, communityId)
                if (alreadyFollows) throw AlreadyRelatedException("This user already follows this community")

                val response = communityRepository.insertFollower(userId, communityId)
                call.respond(response)
            } catch (e: Exception) {
                sendErrorResponse(call, e)
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
                communityRepository.deleteFollower(communityId.toInt(), userId)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        get("{id}/followers") {
            val communityId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode. BadRequest, "missing community id")
            try {
                val followers = communityRepository.fetchFollowers(communityId.toInt())
                call.respond(followers ?: emptyList())
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        patch("{id}") {
            try {
                val userId = validator.checkAuth(call)
                val community = call.receive<Community>()
                val communityIdStr = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "missing community id")

                val communityId = communityIdStr.toInt()
                val comm = communityRepository.fetchCommunity(communityId) ?: throw ResourceNotFoundException()
                if (comm.admin != userId)
                    throw UnauthorizedException()
                val updatedCommunity =
                    communityRepository.updateCommunity(communityId, community) ?: throw ResourceNotFoundException("Community not found")
                call.respond(updatedCommunity)
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        post {
            try {
                val id = validator.checkAuth(call)

                val community = call.receive<Community>()
                if (id != community.admin)
                    throw UnauthorizedException("Can't create community in the name of another user")
                if (community.name == null || !nameIsValid(community.name))
                    throw InvalidNameException()

                if (community.contacts.isEmpty())
                    throw InvalidBodyException("A community needs at least one contact")

                if (community.contacts.size > 3)
                    throw InvalidBodyException("A community can't have more than 3 contacts")

                if (communityRepository.alreadyExists(community.name))
                    throw AlreadyExistsException("A community with this name already exists")

                val createdCommunity = communityRepository.insertCommunity(community)
                communityRepository.insertFollower(community.admin, createdCommunity.id)

                call.respond(createdCommunity)
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        post("{communityId}/contacts") {
            try {
                val userId = validator.checkAuth(call)
                val contacts = call.receive<List<Contact>>()
                val communityId = call.parameters["communityId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "missing community id")
                val community = communityRepository.fetchCommunity(communityId.toInt()) ?: throw ResourceNotFoundException("Community not found")
                if (community.admin != userId)
                    throw UnauthorizedException()
                if (community.contacts.size + contacts.size > 3)
                    throw InvalidBodyException("A community can't have more than 3 contacts")
                for (contact in contacts) {
                    if (contact.title.isNullOrEmpty())
                        throw InvalidBodyException("A contact must have a title")
                }
                val response = communityRepository.insertContacts(contacts, community.id)
                call.respond(response)
            }
            catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        patch("{communityId}/contacts/{contactId}") {
            try {
                val userId = validator.checkAuth(call)
                val contact = call.receive<Contact>()
                val communityId = call.parameters["communityId"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "missing community id")
                val community = communityRepository.fetchCommunity(communityId.toInt()) ?: throw ResourceNotFoundException("Community not found")
                if (community.admin != userId)
                    throw UnauthorizedException()
                if (contact.title.isNullOrEmpty())
                    throw InvalidBodyException("A contact must have a title")
                val response = communityRepository.updateContact(contact, community.id) ?: throw ResourceNotFoundException("Contact not found")

                call.respond(response)
            }
            catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        delete("{communityId}/contacts/{contactId}") {
            try {
                val userId = validator.checkAuth(call)
                val contact = call.receive<Contact>()
                val communityId = call.parameters["communityId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "missing community id")
                val community = communityRepository.fetchCommunity(communityId.toInt()) ?: throw ResourceNotFoundException("Community not found")
                if (community.admin != userId)
                    throw UnauthorizedException()
                communityRepository.deleteContact(contact, community.id) ?: throw ResourceNotFoundException("Contact not found")
                call.respond(HttpStatusCode.OK)
            }
            catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }
    }
}
