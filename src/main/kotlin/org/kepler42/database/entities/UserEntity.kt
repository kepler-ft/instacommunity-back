package org.kepler42.database.entities

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.kepler42.models.*

// object is the representation of the Table users
object Users : IdTable<String>("users") {
    override val id = entityId("id", varchar("id", 100))
    val name = varchar("name", 200)
    val username = varchar("username", 200)
    val occupation = varchar("occupation", 200)
    val email = varchar("email", 200)
    val usePhoto = bool("use_photo")
}

// this class represents the row from the table
class UserEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, UserEntity>(Users)

    var name by Users.name
    var username by Users.username
    var occupation by Users.occupation
    var email by Users.email
    var usePhoto by Users.usePhoto
    var communities by CommunityEntity via UsersCommunities

    fun toModel(): User {
        return User(
            this.id.value,
            this.name,
            this.username,
            this.occupation,
            this.usePhoto,
            this.email)
    }
}
