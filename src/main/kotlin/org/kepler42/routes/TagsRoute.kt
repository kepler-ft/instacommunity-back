package org.kepler42.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kepler42.database.repositories.TagRepository
import org.kepler42.utils.getHttpCode
import org.koin.ktor.ext.inject

fun Route.tagRoute() {
    val tagRepository: TagRepository by inject()
    route("/tags") {
        get {
            try {
                val tags = tagRepository.getAll()
                call.respond(tags)
            } catch (e: Exception) {
                call.respond(getHttpCode(e))
            }
        }
    }
}
