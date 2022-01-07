package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.controllers.UserController
import org.kepler42.models.User
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject


fun Route.userRoute() {
    val userController: UserController by inject()

    route("/users") {
        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
            try {
                call.respond(userController.getById(id))
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        post {
            val user = call.receive<User>()
            try {
                call.respond(userController.createUser(user))
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        put {
            val user = call.receive<User>()
            try {
                call.respond(userController.updateUser(user))
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        get("{id}/communities") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id.")
            try {
                call.respond(userController.handleGetIdCommunities(id))
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
