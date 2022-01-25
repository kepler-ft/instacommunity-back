package org.kepler42.models

import kotlinx.serialization.Serializable

enum class CommunityType {
    OPEN,
    MODERATED,
    MANAGED
}

@Serializable
data class Community(
    val id: Int = 0,
    val name: String? = null,
    val description: String? = null,
    val contact: String? = null,
    val contact2: String? = null,
    val contact3: String? = null,
    val admin: String? = null,
    val slug: String? = null,
    val photo_url: String? = null,
    val type: CommunityType? = null,
    val contacts: List<Contact>
)
