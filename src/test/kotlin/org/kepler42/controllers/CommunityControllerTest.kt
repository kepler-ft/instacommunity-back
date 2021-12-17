package org.kepler42.controllers

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.kepler42.models.Community
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object CommunityControllerTest : Spek({
    val fakeCommunityRepository = mockk<CommunityRepository>()
    every { fakeCommunityRepository.fetchCommunity(1) } returns Community(1, "Getting Started")
    val controller by memoized { CommunityController(fakeCommunityRepository) }

    describe("Communities controller") {
        it("fetches a community by id") {
            val community = controller.getById(1)
            community shouldNotBe null
            community.name shouldBe "Getting Started"
        }

        it("fetches a community by id") {
            val community = controller.getById(1)
            community shouldNotBe null
            community.name shouldBe "Getting Started"
        }
    }
})
