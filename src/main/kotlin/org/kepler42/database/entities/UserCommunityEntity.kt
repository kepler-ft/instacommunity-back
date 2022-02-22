package org.kepler42.database.entities

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.kepler42.models.UserCommunity

// object that represents a table from the database
object UsersCommunities : IntIdTable("communities_followers") {
    val user_id = reference("user_id", UsersTable)
    val community_id = reference("community_id", CommunitiesTable)
}

// object that represents a line(register) from the database
class UserCommunityEntity(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, UserCommunityEntity>(UsersCommunities)

    var user_id by UsersCommunities.user_id
    var community_id by UsersCommunities.community_id

    fun toModel(): UserCommunity {
        return UserCommunity(this.id.value, this.user_id.value, this.community_id.value)
    }
}
