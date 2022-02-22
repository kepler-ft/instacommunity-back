package org.kepler42.database.entities

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.kepler42.models.Contact
import org.kepler42.models.User

// object is the representation of the Table users
object UsersTable : IdTable<String>("users") {
    val idColumn = varchar("id", 100)
    override val id = idColumn.entityId()
    val name = varchar("name", 200)
    val username = varchar("username", 200)
    val occupation = varchar("occupation", 200)
    val about = varchar("about", 200).nullable()
    val email = varchar("email", 200)
    val photoURL = varchar("photo_url",500).nullable()
    val contact_title = varchar("contact_title",200).nullable()
    val contact_link = varchar("contact_link",200).nullable()
}

// this class represents the row from the table
class UserEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, UserEntity>(UsersTable)

    var name by UsersTable.name
    var username by UsersTable.username
    var occupation by UsersTable.occupation
    var about by UsersTable.about
    var email by UsersTable.email
    var photoURL by UsersTable.photoURL
    var communities by CommunityEntity via UsersCommunities
    var contact_title by UsersTable.contact_title
    var contact_link by UsersTable.contact_link

    fun toModel(): User {
        return User(
            this.id.value,
            this.name,
            this.username,
            this.occupation,
            this.about,
            this.photoURL,
            this.email,
            Contact(
                title = this.contact_title,
                link = this.contact_link
            )
        )
    }
}
