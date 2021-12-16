package org.kepler42.controllers

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.kepler42.errors.InvalidNameException
import org.kepler42.models.User
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

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
            val user = controller.createUser(User(name = "Ada"))
            user.name shouldBe "Ada"
        }
    }
})
