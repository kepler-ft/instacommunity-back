package org.kepler42.database.entities

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.kepler42.models.*

// object is the representation of the Table users
object Users : IntIdTable("users") {
    val name = varchar("name", 200)
}

// this class represents the row from the table
class UserEntity(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, UserEntity>(Users)

    var name by Users.name
    var communities by CommunityEntity via UsersCommunities

    fun toModel(): User {
        return User(this.id.value, this.name)
    }
}
