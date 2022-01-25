package org.kepler42.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Concat

@Serializable data class User(
    val id: String? = null,
    val name: String? = null,
    val username: String? = null,
    val occupation: String? = null,
    val photoURL: String? = null,
    val email: String? = null,
    val contact: Contact? = null,
)
