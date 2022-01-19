package org.kepler42.routes

import com.google.firebase.auth.FirebaseAuth
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.controllers.UserController
import org.kepler42.errors.UnauthorizedException
import org.kepler42.models.User
import org.kepler42.utils.TokenValidator
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject


fun Route.userRoute() {
    val userController: UserController by inject()
    val validator: TokenValidator by inject()

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
                validator.checkAuth(call)
                val user = call.receive<User>()
                call.respond(userController.createUser(user))
            } catch (e: Exception) {
                call.respond(getHttpCode(e), mapOf("error" to e.message))
            }
        }

        put("{id}") {
            try {
                val authId = validator.checkAuth(call)
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id in URL"))
                if (authId != id)
                    throw UnauthorizedException("Can't change other users info")
                val user = call.receive<User>()
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
