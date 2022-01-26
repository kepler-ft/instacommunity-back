package org.kepler42.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.utils.getHttpCode
import org.kepler42.controllers.*
import org.koin.ktor.ext.inject

fun Route.tagRoute() {
    val tagController: TagController by inject()
    route("/tags") {
        get {
            try {
                val tags = tagController.getAll()
            } catch (e: Exception) {
                call.respond(getHttpCode(e))
            }
        }
    }
}
