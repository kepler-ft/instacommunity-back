package org.kepler42.models

import kotlinx.serialization.Serializable

@Serializable
data class Tag (
    val id: Int? = 0,
    val name: String? = null,
    )
