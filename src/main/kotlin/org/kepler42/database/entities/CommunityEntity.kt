package org.kepler42.database.entities

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

import org.kepler42.models.*

// Object is the representation of communities table
object Communities : IntIdTable("communities") {
	val name = varchar("name", 200)
	val description = text("description")
}

// This class represents a row from the table
class CommunityEntity(id: EntityID<Int>): Entity<Int>(id) {
	companion object : EntityClass<Int, CommunityEntity>(Communities)

	var name by Communities.name
	var description by Communities.description

	fun toModel(): Community {
		return Community(this.id.value, this.name, this.description)
	}
}
