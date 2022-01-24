package org.kepler42.utils

import com.google.firebase.auth.FirebaseAuthException
import io.ktor.http.*
import org.kepler42.controllers.UnknownErrorException
import org.kepler42.errors.*

private val values = mapOf(
    AlreadyRelatedException::class to 400,
    NumberFormatException::class to 400,
    UnauthorizedException::class to 401,
    FirebaseAuthException:: class to 401,
    InvalidNameException::class to 406,
    ResourceNotFoundException::class to 404,
    AlreadyExistsException::class to 409,
    UnknownErrorException::class to 500,
)

fun getHttpCode(e: Exception): HttpStatusCode {
    val value = values[e::class]
    return if (value == null) {
        e.printStackTrace()
        HttpStatusCode.InternalServerError
    }
    else
        HttpStatusCode.fromValue(value)
}
