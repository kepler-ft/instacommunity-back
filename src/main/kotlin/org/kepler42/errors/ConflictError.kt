package org.kepler42.errors

import kotlinx.serialization.Serializable

@Serializable
data class ConflictError(
    val message: String = "Conflict"): Error(409, message)
