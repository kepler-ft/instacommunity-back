package org.kepler42.database.repositories

import org.kepler42.models.User
import org.kepler42.database.entities.UserEntity
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*

interface UserRepository {
    fun getUserById(id: Int): User
    fun insertUser(user: User): User
}

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
