package org.kepler42.database.entities

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

import org.kepler42.models.*

// object that represents a table from the database
object UsersCommunities : IntIdTable("users_communities") {
	val user_id = integer("user_id")
	val community_id = integer("community_id")
}

// object that represents a line(register) from the database
class UserCommunityEntity(id: EntityID<Int>): Entity<Int>(id) {
    companion object : EntityClass<Int, UserCommunityEntity>(UsersCommunities)

    var user_id by UsersCommunities.user_id
    var community_id by UsersCommunities.community_id

    fun toModel(): UserCommunity {
        return UserCommunity(this.id.value, this.user_id, this.community_id)
    }
}