package org.kepler42.utils

import io.ktor.application.*
import io.ktor.response.*

suspend fun sendErrorResponse(call: ApplicationCall, e: Exception) {
    val code = getHttpCode(e)
    val message = mapOf("error" to e.message)
    call.respond(status = code, message)
}
