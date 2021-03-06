package org.kepler42.models

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: Int? = 0,
    val title: String? = null,
    val link: String? = null
) {
}
