package org.kepler42.database.repositories

import org.jetbrains.exposed.dao.id.EntityID
import org.kepler42.models.User
import org.kepler42.database.entities.UserEntity
import org.kepler42.controllers.UserRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.kepler42.database.entities.CommunityEntity
import org.kepler42.database.entities.Users
import org.kepler42.database.entities.Users.id
import org.kepler42.models.Community

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
            Users.insert {
                it[id] = user.id
                it[name] = user.name!!
                it[username] = user.username!!
                it[email] = user.email!!
                it[occupation] = user.occupation!!
                it[usePhoto] = user.usePhoto!!
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
            user.usePhoto?.let { oldUser?.usePhoto = user.usePhoto}
            oldUser?.toModel()
        }
    }
}
