package org.kepler42.database.repositories

import org.kepler42.models.User
import org.kepler42.database.entities.UserEntity
import org.kepler42.controllers.UserRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*

class UserRepositoryImpl: UserRepository {
    override fun getUserById(id: Int): User {
        TODO("Not yet implemented")
    }
    override fun insertUser(user: User): User {
        val newUser = transaction {
            addLogger(StdOutSqlLogger)
            UserEntity.new { name = user.name!! }
        }
        return newUser.toModel()
    }

}
