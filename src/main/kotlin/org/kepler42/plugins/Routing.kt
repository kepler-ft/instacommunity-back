package org.kepler42.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*

import org.kepler42.routes.*
import org.kepler42.controllers.*

fun Application.configureRouting() {
    routing {
        communityRoute()
        userRoute()
        tagRoute()
    }
}
