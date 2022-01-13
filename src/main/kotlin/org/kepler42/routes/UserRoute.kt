package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.controllers.UserController
import org.kepler42.errors.UnauthorizedException
import org.kepler42.models.User
import org.kepler42.utils.checkAuth
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject


fun Route.userRoute() {
    val userController: UserController by inject()

    route("/users") {
        get {
            try {
                val username = call.request.queryParameters["username"]
                if (username != null)
                    call.respond(userController.getByUsername(username))
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
            try {
                call.respond(userController.getById(id))
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        post {
            try {
                checkAuth(call)
                val user = call.receive<User>()
                call.respond(userController.createUser(user))
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        put("{id}") {
            try {
                val id = checkAuth(call)
                val user = call.receive<User>()
                if (id != user.id)
                    throw UnauthorizedException("Can't change other users info")
                call.respond(userController.updateUser(user))
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        get("{id}/communities") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id.")
            try {
                call.respond(userController.getFollowedCommunities(id))
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }
    }
}
