package org.kepler42.routes

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*

import org.kepler42.models.Greeting

fun Route.greetingRoute() {
    route("/greeting") {
        get ("{id?}") {
            call.respond(Greeting(0, "Hello, Kepler!"))
        }
    }
}
