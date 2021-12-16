package org.kepler42.utils

import io.ktor.http.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.controllers.CannotFetchException
import org.kepler42.controllers.CannotInsertException
import org.kepler42.controllers.UnknownErrorException
import org.kepler42.database.entities.CommunitiesTable.description
import org.kepler42.errors.*

fun getHttpCode(e: Exception): HttpStatusCode {
    val statusCode: Int = when (e) {
        is InvalidNameException -> 406
        is UnknownErrorException -> 500
        is CannotInsertException -> 500
        is CannotFetchException -> 500
        is ExposedSQLException -> 500
        is AlreadyRelatedException -> 404
        else -> 500
    }
    return HttpStatusCode.fromValue(statusCode)
}
