package org.kepler42.controllers

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.kepler42.errors.InvalidNameException
import org.kepler42.models.User
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

fun generateString(length: Int): String {
    val charset = ('a' .. 'z')
    return List(length) { charset.random() }
        .joinToString("")
}

object UserControllerTest : Spek({
    val fakeUserRepository = mockk<UserRepository>()
    val userSlot = slot<User>()
    every { fakeUserRepository.insertUser(capture(userSlot)) } answers { userSlot.captured }
    val controller by memoized { UserController(fakeUserRepository) }

    describe("User controller") {
        it("throws when name is smaller than 2 letters") {
            shouldThrowExactly<InvalidNameException> {
                controller.createUser(User(name = "a"))
            }
        }

        it("doesn't throw when name have 2 or more letters") {
            shouldNotThrow<InvalidNameException> {
                controller.createUser(User(name = "Ada"))
            }
        }

        it("throws when user name has more than 200 letters") {
            val bigName = generateString(201)
            shouldThrowExactly<InvalidNameException> {
                controller.createUser(User(name = bigName))
            }
        }
    }
})
