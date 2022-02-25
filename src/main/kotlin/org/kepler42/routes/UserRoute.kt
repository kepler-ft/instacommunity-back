package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.database.repositories.CommunityRepository
import org.kepler42.database.repositories.UserRepository
import org.kepler42.errors.AlreadyExistsException
import org.kepler42.errors.InvalidNameException
import org.kepler42.errors.ResourceNotFoundException
import org.kepler42.errors.UnauthorizedException
import org.kepler42.models.User
import org.kepler42.utils.TokenValidator
import org.kepler42.utils.getHttpCode
import org.kepler42.utils.sendErrorResponse
import org.koin.ktor.ext.inject

private fun invalidName(name: String?) =
    when {
        (name == null) -> true
        (name.length < 2) -> true
        (name.length > 200) -> true
        else -> false
    }

fun Route.userRoute() {
    val userRepository: UserRepository by inject()
    val communityRepository: CommunityRepository by inject()
    val validator: TokenValidator by inject()

    route("/users") {
        get {
            try {
                val username = call.request.queryParameters["username"]
                if (username != null)
                {
                    val user = userRepository.getByUsername(username) ?: throw ResourceNotFoundException()
                    call.respond(user)
                }
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
            try {
                val user = userRepository.getUserById(id) ?: throw ResourceNotFoundException()
                call.respond(user)
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        post {
            try {
                val id = validator.checkAuth(call)
                val user = call.receive<User>()
                if (id == user.id)
                {
                    if (invalidName(user.name))
                        throw InvalidNameException()
                    if (user.username == null)
                        throw IllegalArgumentException("username cannot be null")
                    val existingUser = userRepository.getByUsername(user.username)
                    if (existingUser != null)
                        throw AlreadyExistsException("this username was already taken")

                    val createdUser = userRepository.insertUser(user)
                    call.respond(createdUser)
                }
                else
                    throw UnauthorizedException()
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        put("{id}") {
            try {
                val authId = validator.checkAuth(call)
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id in URL"))
                if (authId != id)
                    throw UnauthorizedException("Can't change other users info")
                val user = call.receive<User>()
                if (invalidName(user.name))
                    throw InvalidNameException()

                val updatedUser = userRepository.changeUser(user) ?: throw ResourceNotFoundException()
                call.respond(updatedUser)
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }

        get("{id}/communities") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id.")
            try {
                val communities = communityRepository.fetchCommunitiesFollowedByUser(id) ?: emptyList()
                call.respond(communities)
            } catch (e: Exception) {
                sendErrorResponse(call, e)
            }
        }
    }
}
