package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.database.operations.*
import org.kepler42.models.*

private fun invalidName(name: String?) =
        when {
            (name == null) -> true
            (name.length < 2) -> true
            (name.length > 200) -> true
            else -> false
        }

fun Route.userRoute() {
    route("/users") {
        post() {
            val user = call.receive<User>()
            if (invalidName(user.name))
                    return@post call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid name")
                    )

            try {
                call.respond(insertUsers(user))
            } catch (e: ExposedSQLException) {
                call.respond(mapOf("error" to "Something has gone pretty bad"))
            }
        }

        get("{id}/communities") {
            val id = call.parameters["id"]
            val communities = fetchCommunitiesByUserId(id!!.toInt())
            call.respond(communities ?: emptyList())
        }
    }
}
