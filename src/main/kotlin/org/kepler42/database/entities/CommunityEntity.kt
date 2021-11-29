package org.kepler42.database.entities

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

import org.kepler42.models.*

// Object is the representation of communities table
object CommunitiesTable : IntIdTable("communities") {
	val name = varchar("name", 200)
	val description = text("description")
	val contact = varchar("contact", 200)
	val contact2 = varchar("contact2", 200).nullable()
	val contact3 = varchar("contact3", 200).nullable()
}

// This class represents a row from the table
class CommunityEntity(id: EntityID<Int>): Entity<Int>(id) {
	companion object : EntityClass<Int, CommunityEntity>(CommunitiesTable)

	var name by CommunitiesTable.name
	var description by CommunitiesTable.description
	var followers by UserEntity via UsersCommunities
	var contact by CommunitiesTable.contact
	var contact2 by CommunitiesTable.contact2
	var contact3 by CommunitiesTable.contact3

	fun toModel(): Community {
		return Community(this.id.value, this.name, this.description, this.contact, this.contact2, this.contact3)
	}
}
