package org.kepler42.models

import kotlinx.serialization.Serializable

@Serializable
data class Community(
    val id: Int = 0,
    val name: String? = null,
    val description: String? = null,
    val contact: String? = null,
)
