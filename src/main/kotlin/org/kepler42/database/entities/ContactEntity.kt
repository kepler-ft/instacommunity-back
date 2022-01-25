package org.kepler42.database.entities

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.kepler42.models.Contact

object ContactsTable: IntIdTable("community_contacts") {
    val title = varchar("title", 200)
    val link = varchar("link", 200).nullable()
    var community = reference("community_id", CommunitiesTable)
}

class ContactEntity(id: EntityID<Int>): Entity<Int>(id) {
    companion object : EntityClass<Int, ContactEntity>(ContactsTable)

    var title by ContactsTable.title
    var link by ContactsTable.link
    var community by CommunityEntity referencedOn ContactsTable.community

    fun toModel(): Contact {
        return Contact(
            id = this.id.value,
            title = this.title,
            link = this.link,
        )
    }
}
