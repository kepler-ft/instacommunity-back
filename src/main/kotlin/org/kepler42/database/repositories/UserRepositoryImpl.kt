package org.kepler42.database.repositories

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.kepler42.database.entities.UserEntity
import org.kepler42.database.entities.UsersTable
import org.kepler42.database.repositories.utils.insensitiveLike
import org.kepler42.errors.InvalidBodyException
import org.kepler42.models.User

interface UserRepository {
    fun getUserById(id: String): User?
    fun insertUser(user: User): User
    fun changeUser(user: User): User?
    fun getByUsername(username: String): User?
    fun getUsersByName(searchTerm: String): List<User>?
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
                it[about] = user.about
                it[email] = user.email!!
                it[occupation] = user.occupation!!
                it[photoURL] = user.photoURL
                it[contact_link] = user.contact?.link
                it[contact_title] = user.contact?.title
            }
        }
        return user
    }

    override fun changeUser(user: User): User? {
        return transaction {
            addLogger(StdOutSqlLogger)
            if (user.id == null)
                throw InvalidBodyException()
            val oldUser = user.id.let { UserEntity.findById(it) } ?: return@transaction null
            user.name?.let { oldUser.name = user.name }
            user.occupation?.let { oldUser.occupation = user.occupation }
            user.about?.let { oldUser.about = user.about }
            user.photoURL?.let { oldUser.photoURL = user.photoURL}
            user.contact?.let {
                oldUser.contact_link = user.contact.link
                oldUser.contact_title = user.contact.title
            }
            oldUser.toModel()
        }
    }

    override fun getByUsername(username: String): User? {
        val user = transaction {
            addLogger(StdOutSqlLogger)
            UserEntity.find(UsersTable.username eq username).firstOrNull()
        }
        return user?.toModel()
    }

    override fun getUsersByName(searchTerm: String): List<User>? {
        val usersList = transaction {
            addLogger(StdOutSqlLogger)
            UserEntity.find(UsersTable.username insensitiveLike "%$searchTerm%").map { it.toModel() }
        }
        return usersList
    }
}
