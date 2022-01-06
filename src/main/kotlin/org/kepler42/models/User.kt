package org.kepler42.models

import kotlinx.serialization.Serializable

@Serializable data class User(
    val id: Int = 0,
    val googleId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val nickName: String? = null,
    val jobPost: String? = null,
    val usePhoto: Boolean? = null,
    val email: String? = null,
)
