package org.kepler42.database.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object CommunitiesModeratorsTable: IntIdTable("communities_moderators") {
    val user_id = reference("user_id", UsersTable)
    val community_id = reference("community_id", CommunitiesTable)
}
