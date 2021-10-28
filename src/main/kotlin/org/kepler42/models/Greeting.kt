package org.kepler42.models

import kotlinx.serialization.Serializable

@Serializable // Serialization for getting data in json
data class Greeting(
    val id: Long,
    val phrase: String
)