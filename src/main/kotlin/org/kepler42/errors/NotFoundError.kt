package org.kepler42.errors

import kotlinx.serialization.Serializable

@Serializable
data class NotFoundError(
    val message: String = "Resource not found"): Error(404, message)
