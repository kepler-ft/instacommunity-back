package org.kepler42.database.entities

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object CommunitiesTagsTable : IntIdTable("communities_tags") {
    val tag_id = reference("tag_id", TagsTable)
    val community_id = reference("community_id", CommunitiesTable)
}

class CommunitiesTagsEntity(id: EntityID<Int>): Entity<Int>(id) {
    companion object : EntityClass<Int, CommunitiesTagsEntity>(CommunitiesTagsTable)

    var tag_id by CommunitiesTagsTable.tag_id
    var community_id by CommunitiesTagsTable.community_id
}
