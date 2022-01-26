package org.kepler42.database.entities

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.kepler42.models.Tag

object TagsTable : IntIdTable("communities_tags") {
    val name = varchar("name", 200)
    val community = reference("community_id", CommunitiesTable)
}
class TagEntity(id: EntityID<Int>): Entity<Int>(id) {
    companion object : EntityClass<Int, TagEntity>(TagsTable)

    var name by TagsTable.name
    var communities by CommunityEntity via CommunitiesTagsTable

    fun toModel(): Tag {
        return Tag(
            id = this.id.value,
            name = this.name,
        )
    }
}
