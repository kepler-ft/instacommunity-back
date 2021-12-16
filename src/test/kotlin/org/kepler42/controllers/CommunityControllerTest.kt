package org.kepler42.controllers

import com.typesafe.config.ConfigException
import io.mockk.mockk
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import org.kepler42.controllers.CommunityRepository
import org.kepler42.database.repositories.UserRepository
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.kepler42.models.User
import org.kepler42.errors.InvalidNameException
import org.kepler42.models.Community
import kotlin.test.*


object CommunityControlerSpek: Spek({
    val fakeCommunityRepository = mockk<CommunityRepository>()
    every { fakeCommunityRepository.fetchCommunity(1) } returns Community(1, "Getting Started")
    val controller by memoized { CommunityController(fakeCommunityRepository)}

    describe("Communities controller") {
        it("fetches a community by id") {
            val community = controller.getById(1)
            community shouldNotBe null
            val communityName = community?.name
            communityName shouldBe "Getting Started"
        }
    }
})
