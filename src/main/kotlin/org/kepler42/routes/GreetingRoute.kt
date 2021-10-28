package org.kepler42.routes

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*

import org.kepler42.models.Greeting
import org.kepler42.database.fetchGreeting

fun Route.greetingRoute() {
    route("/greeting") {
        get ("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing or malformed id.",
                status = HttpStatusCode.BadRequest
            )
            val greeting = fetchGreeting(id.toLong())
            call.respond(greeting!!)
        }
    }
}
