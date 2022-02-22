package org.kepler42.database.entities

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SortOrder
import org.kepler42.models.Community
import org.kepler42.models.CommunityType

// Object is the representation of communities table
object CommunitiesTable : IntIdTable("communities") {
    val name = varchar("name", 200)
    val description = text("description")
    val ademiro = reference("admin", UsersTable)
    val photo_url = varchar("photo_url", 500)
    val slug = varchar("slug", 200)
    val type = enumerationByName("type", 9, CommunityType::class)
}

// This class represents a row from the table
class CommunityEntity(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, CommunityEntity>(CommunitiesTable)

    var name by CommunitiesTable.name
    var description by CommunitiesTable.description
    var followers by UserEntity via UsersCommunities
    var moderators by UserEntity via CommunitiesModeratorsTable
    var admin by CommunitiesTable.ademiro
    var photo_url by CommunitiesTable.photo_url
    var type by CommunitiesTable.type
    val contacts by ContactEntity referrersOn ContactsTable.community
    var tags by TagEntity via CommunitiesTagsTable
    var slug by CommunitiesTable.slug

    fun toModel(): Community {
        return Community(
            id = this.id.value,
            name = this.name,
            description = this.description,
            admin = this.admin.value,
            contacts = this.contacts
                .orderBy(ContactsTable.id to SortOrder.ASC)
                .map { it.toModel() },
            photoURL = this.photo_url,
            slug = this.slug,
            type = this.type)
    }
}
