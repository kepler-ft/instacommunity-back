package org.kepler42.controllers

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.database.operations.*
import org.kepler42.errors.InvalidNameException
import org.kepler42.models.*

interface UserRepository {

}

data class CannotInsertException(
    override val message: String = "Failed to insert"
): Exception(message)

data class CannotFetchException(
    override val message: String = "Failed to fetch"
): Exception(message)

data class UnknownErrorException(
    override val message: String = "Unknown internal error"
): Exception(message)

class UserController {
    private fun invalidName(name: String?) =
        when {
            (name == null) -> true
            (name.length < 2) -> true
            (name.length > 200) -> true
            else -> false
        }

    fun handlePost(user: User): User {
        if (invalidName(user.name))
            throw InvalidNameException()

        return try {
            insertUser(user)
        } catch (e: ExposedSQLException) {
            throw CannotInsertException()
        } catch (e: Exception) {
            throw UnknownErrorException()
        }
    }

    fun handleGetIdCommunities(id: Int): List<Community> {
        return try {
            fetchCommunitiesByUserId(id) ?: emptyList()
        } catch(e: ExposedSQLException) {
            throw CannotFetchException()
        } catch (e: Exception) {
            throw UnknownErrorException()
        }
    }
}
