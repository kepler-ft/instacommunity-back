package org.kepler42.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*

import org.kepler42.models.Greeting

fun Application.configureRouting() {

    routing {
        get("/") {
                call.respond(Greeting(1, "Hello, Kepler!"))
            }
    }
}
