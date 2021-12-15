package org.kepler42.controllers

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.kepler42.database.repositories.UserRepository
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.kepler42.models.User
import org.kepler42.errors.InvalidNameException
import kotlin.test.*

object UserControllerSpek: Spek({
    val fakeUserRepository = mockk<UserRepository>()
    val userSlot = slot<User>()
    every { fakeUserRepository.insertUser(capture(userSlot)) } answers { userSlot.captured }
    val controller by memoized { UserController(fakeUserRepository) }

    describe("User controller") {
        it("throws when name is smaller than 2 letters") {
            assertFailsWith<InvalidNameException> {
                controller.createUser(User(name = "a"))
            }
        }

        it("doesn't throw when name have 2 or more letters") {
            val user = controller.createUser(User(name = "Ada"))
            user.name shouldBe "Ada"
        }
    }
})
