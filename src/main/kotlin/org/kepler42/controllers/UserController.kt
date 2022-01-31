package org.kepler42.controllers

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.kepler42.database.repositories.UserRepository
import org.kepler42.errors.AlreadyExistsException
import org.kepler42.errors.InvalidNameException
import org.kepler42.errors.ResourceNotFoundException
import org.kepler42.models.*
import java.lang.IllegalArgumentException

data class CannotInsertException(
    override val message: String = "Failed to insert"
): Exception(message)

data class CannotFetchException(
    override val message: String = "Failed to fetch"
): Exception(message)

data class UnknownErrorException(
    override val message: String = "Unknown internal error"
): Exception(message)

class UserController(private val userRepository: UserRepository, private val communityRepository: CommunityRepository) {
    private fun invalidName(name: String?) =
        when {
            (name == null) -> true
            (name.length < 2) -> true
            (name.length > 200) -> true
            else -> false
        }

    fun createUser(user: User): User {
        if (invalidName(user.name))
            throw InvalidNameException()
        if (user.username == null)
            throw IllegalArgumentException("username cannot be null")
        val existingUser = userRepository.getByUsername(user.username)
        if (existingUser != null)
            throw AlreadyExistsException("this username was already taken")

        return userRepository.insertUser(user)
    }

    fun getById(googleId: String): User {
        return userRepository.getUserById(googleId) ?: throw ResourceNotFoundException()
    }

    fun updateUser(user: User): User {
        if (invalidName(user.name))
            throw InvalidNameException()

        return userRepository.changeUser(user) ?: throw ResourceNotFoundException()
    }

    fun getFollowedCommunities(id: String): List<Community> {
        return communityRepository.fetchCommunitiesFollowedByUser(id) ?: emptyList()
    }

    fun getByUsername(username: String): User {
        return userRepository.getByUsername(username) ?: throw ResourceNotFoundException()
    }

    fun getUsersByName(searchTerm: String): List<User> {
        return userRepository.getUsersByName(searchTerm) ?: emptyList()
    }
}
