package org.kepler42.errors

import kotlinx.serialization.Serializable

@Serializable
data class InternalServerError(
    val message: String = "Internal server error"): Error(500, message)
