package org.kepler42.database

import org.jetbrains.exposed.sql.*

object Greetings : Table() {
    val id = long("id").autoIncrement()
    val phrase = varchar("phrase", length = 50)

    override val primaryKey = PrimaryKey(id)
}