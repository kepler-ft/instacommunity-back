package org.kepler42.models

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: Int,
    val title: String?,
    val link: String?
) {
}
