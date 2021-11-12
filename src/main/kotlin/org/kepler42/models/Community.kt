package org.kepler42.models

import kotlinx.serialization.Serializable

@Serializable
data class Community(val id: Int = 0, val name: String, val description: String)
