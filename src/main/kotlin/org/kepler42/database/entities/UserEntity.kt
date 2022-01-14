package org.kepler42.database.entities

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.kepler42.models.*

// object is the representation of the Table users
object UsersTable : IdTable<String>("users") {
    val idColumn = varchar("id", 100)
    override val id = idColumn.entityId()
    val name = varchar("name", 200)
    val username = varchar("username", 200)
    val occupation = varchar("occupation", 200)
    val email = varchar("email", 200)
    val usePhoto = bool("use_photo")
    val photoURL = varchar("photo_url",200)
}

// this class represents the row from the table
class UserEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, UserEntity>(UsersTable)

    var name by UsersTable.name
    var username by UsersTable.username
    var occupation by UsersTable.occupation
    var email by UsersTable.email
    var usePhoto by UsersTable.usePhoto
    var photoURL by UsersTable.photoURL
    var communities by CommunityEntity via UsersCommunities

    fun toModel(): User {
        return User(
            this.id.value,
            this.name,
            this.username,
            this.occupation,
            this.usePhoto,
            this.photoURL,
            this.email)
    }
}
