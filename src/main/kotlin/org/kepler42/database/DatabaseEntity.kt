package org.kepler42.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

import org.kepler42.models.Greeting

object Greetings : IntIdTable() {
    val phrase = varchar("phrase", length = 50)
}

class GreetingEntity(id: EntityID<Int>): Entity<Int>(id) {
    companion object : EntityClass<Int, GreetingEntity>(Greetings)

    var phrase by Greetings.phrase
    fun toModel(): Greeting {
        return Greeting(this.id.value.toLong(), this.phrase)
    }
}
