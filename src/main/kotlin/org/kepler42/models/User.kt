package org.kepler42.models

import kotlinx.serialization.Serializable

@Serializable data class User(
    val id: String? = null,
    val name: String? = null,
    val username: String? = null,
    val occupation: String? = null,
    val usePhoto: Boolean? = null,
    val email: String? = null,
)
