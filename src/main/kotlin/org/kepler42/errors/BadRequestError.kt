package org.kepler42.errors

import kotlinx.serialization.Serializable

@Serializable
data class BadRequestError(
    val message: String = "Bad request"
): Error(400, message)
