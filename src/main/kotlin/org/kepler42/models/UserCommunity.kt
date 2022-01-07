package org.kepler42.models

import kotlinx.serialization.Serializable

@Serializable data class UserCommunity(val id: Int = 0, val userId: String, val communityId: Int)
