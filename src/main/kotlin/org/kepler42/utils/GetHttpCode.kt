package org.kepler42.utils

import io.ktor.http.*
import org.flywaydb.core.internal.resource.ResourceName
import org.kepler42.errors.*

private val values = mapOf(
    AlreadyRelatedException::class to 400,
    InvalidNameException::class to 406,
    ResourceNotFoundException::class to 404,
)

fun getHttpCode(e: Exception): HttpStatusCode {
    val value = values[e::class]
    return if (value == null)
        HttpStatusCode.InternalServerError
    else
        HttpStatusCode.fromValue(value)
}
