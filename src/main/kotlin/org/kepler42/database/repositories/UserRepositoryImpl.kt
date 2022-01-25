package org.kepler42.database.repositories

import org.kepler42.models.User
import org.kepler42.database.entities.UserEntity
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.kepler42.database.entities.UsersTable

interface UserRepository {
    fun getUserById(id: String): User?
    fun insertUser(user: User): User
    fun changeUser(user: User): User?
    fun getByUsername(username: String): User?
}

class UserRepositoryImpl: UserRepository {
    override fun getUserById(id: String): User? {
        val user = transaction {
            addLogger(StdOutSqlLogger)
            UserEntity.findById(id)
        }
        return user?.toModel()
    }

    override fun insertUser(user: User): User {
        transaction {
            addLogger(StdOutSqlLogger)
            UsersTable.insert {
                it[id] = user.id
                it[name] = user.name!!
                it[username] = user.username!!
                it[email] = user.email!!
                it[occupation] = user.occupation!!
                it[photoURL] = user.photoURL
            }
        }
        return user
    }

    override fun changeUser(user: User): User? {
        return transaction {
            addLogger(StdOutSqlLogger)
            val oldUser = user.id?.let { UserEntity.findById(it) }
            user.name?.let { oldUser?.name = user.name }
            user.occupation?.let { oldUser?.occupation = user.occupation }
            user.photoURL?.let { oldUser?.photoURL = user.photoURL}
            oldUser?.toModel()
        }
    }

    override fun getByUsername(username: String): User? {
        val user = transaction {
            addLogger(StdOutSqlLogger)
            UserEntity.find(UsersTable.username eq username).firstOrNull()
        }
        return user?.toModel()
    }
}
