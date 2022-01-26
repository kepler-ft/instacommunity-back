@file:OptIn(ExperimentalSerializationApi::class)

package org.kepler42.routes;

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kepler42.controllers.CommunityRepository
import org.kepler42.controllers.UserController
import org.kepler42.database.repositories.UserRepository
import org.kepler42.models.Community
import org.kepler42.testUtils.*
import org.kepler42.models.User
import org.kepler42.plugins.configureRouting
import org.kepler42.plugins.configureSerialization
import org.kepler42.utils.TokenValidator
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


// Koin: dependency injection
// Spek: test structure (describe, it)
// Kotest: assertion lib (shouldBe, shouldThrow)

object UserRouteTest : Spek({
    val fakeTokenValidator = spyk<TokenValidator>()
    val fakeUserRepository = spyk<UserRepository>()
    val fakeCommunityRepository = mockk<CommunityRepository>()
    val ada = User( "batatinhafrita123", "Ada Luvlace", "ada" )
    val roberto = User( "leavemealone", "Roberto T. Ishimura", "roberto-ti" )
    val gettingStarted = generateCommunity("Getting Started")
    val kotlinCommunity = generateCommunity("Kotlin")
    val adaCommunities = listOf(gettingStarted)
    val robertoCommunities = listOf(gettingStarted, kotlinCommunity)
    val usernameSlot = slot<String>()

    every { fakeTokenValidator.checkAuth(any()) } answers { "user-id" }

    val userSlot = slot<User>()
    every { fakeUserRepository.insertUser(capture(userSlot)) } answers { userSlot.captured }
    every { fakeUserRepository.changeUser(capture(userSlot)) } answers { userSlot.captured }
    every { fakeUserRepository.getByUsername(capture(usernameSlot)) } answers {
        when (usernameSlot.captured) {
            ada.username!! -> ada
            roberto.username!! -> roberto
            else -> nothing
        }
    }
    val userIdSlot = slot<String>()
    every { fakeUserRepository.getUserById(capture(userIdSlot)) } answers {
        when (userIdSlot.captured) {
            ada.id!! -> ada
            roberto.id!! -> roberto
            else -> nothing
        }
    }
    every { fakeCommunityRepository.fetchCommunitiesFollowedByUser(ada.id!!) } answers { adaCommunities }
    every { fakeCommunityRepository.fetchCommunitiesFollowedByUser(roberto.id!!) } answers { robertoCommunities }

    val testKoinModule = module {
        single { fakeTokenValidator }
        single { UserController(fakeUserRepository, fakeCommunityRepository) }
    }

    describe("User route") {
        it("should find Ada by her username") {
            withTestApplication({
                setupForTesting(this, testKoinModule)
            }) {
                handleRequest(HttpMethod.Get, "/users?username=ada").apply {
                    response.content shouldNotBe null
                    val user = Json.decodeFromString<User>(response.content!!)
                    user shouldBeEqualToComparingFields ada
                }
            }
        }

        it("should find Roberto by his username") {
            withTestApplication({
                setupForTesting(this, testKoinModule)
            }) {
                handleRequest(HttpMethod.Get, "/users?username=roberto-ti").apply {
                    response.content shouldNotBe null
                    val user = Json.decodeFromString<User>(response.content!!)
                    user shouldBeEqualToComparingFields roberto
                }
            }
        }

        // GET /users/08hfau389hdy3u2bfa
        it("should find Ada by her user id") {
            withTestApplication({
                setupForTesting(this, testKoinModule)
            }) {
                handleRequest(HttpMethod.Get, "/users/batatinhafrita123").apply {
                    response.content shouldNotBe null
                    val user = Json.decodeFromString<User>(response.content!!)
                    user shouldBeEqualToComparingFields ada
                }
            }
        }

        it("should find Roberto by his user id") {
            withTestApplication({
                setupForTesting(this, testKoinModule)
            }) {
                handleRequest(HttpMethod.Get, "/users/leavemealone").apply {
                    response.content shouldNotBe null
                    val user = Json.decodeFromString<User>(response.content!!)
                    user shouldBeEqualToComparingFields roberto
                }
            }
        }

        it("should find Ada communities") {
            withTestApplication({
                setupForTesting(this, testKoinModule)
            }) {
                handleRequest(HttpMethod.Get, "/users/batatinhafrita123/communities").apply {
                    response.content shouldNotBe null
                    val communities = Json.decodeFromString<MutableList<Community>>(response.content!!)
                    communities shouldBe adaCommunities
                }
            }
        }

        it("should find Roberto communities") {
            withTestApplication({
                setupForTesting(this, testKoinModule)
            }) {
                handleRequest(HttpMethod.Get, "/users/leavemealone/communities").apply {
                    response.content shouldNotBe null
                    val communities = Json.decodeFromString<MutableList<Community>>(response.content!!)
                    communities shouldBe robertoCommunities
                }
            }
        }
        it("should return 404 when user is not found by it's id") {
            withTestApplication({
                setupForTesting(this, testKoinModule)
            }) {
                handleRequest(HttpMethod.Get, "/users/noid").apply {
                    val content = Json.decodeFromString<ErrorResponse>(response.content!!)
                    content.error shouldBe "This resource was not found"
                    response.status()?.value shouldBe 404
                }
            }
        }

        it("should return 404 when user is not found by it's username") {
            withTestApplication({
                setupForTesting(this, testKoinModule)
            }) {
                handleRequest(HttpMethod.Get, "/users?username=no-one").apply {
                    val content = Json.decodeFromString<ErrorResponse>(response.content!!)
                    content.error shouldBe "This resource was not found"
                    response.status()?.value shouldBe 404
                }
            }
        }
        it("Should insert user into database") {
            withTestApplication ({ setupForTesting(this, testKoinModule) }) {
                val user = User("user-id", "Mary Jane", "mary", "Dummy user")
                handleRequest(HttpMethod.Post, "/users") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(user))
                }.apply {
                    response.content shouldNotBe null
                    val content = Json.decodeFromString<User>(response.content!!)
                    content shouldBe user
                }
                verify { fakeUserRepository.insertUser(user)}
            }
        }
        it("should not insert user if id in auth token is different from id in request body") {
            withTestApplication ({ setupForTesting(this, testKoinModule) }) {
                val user = User("dummyid", "John Doe", "john-d", "Dummy user")
                handleRequest(HttpMethod.Post, "/users") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(user))
                }.apply {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }
        }

        it("should update user in the database") {
            withTestApplication ({ setupForTesting(this, testKoinModule) }) {
                val user = User("user-id", "John Doe", "john-d", "Dummy user")
                handleRequest(HttpMethod.Put, "/users/user-id") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(user))
                }.apply {
                    response.content shouldNotBe null
                    val content = Json.decodeFromString<User>(response.content!!)
                    content shouldBe user
                }
                verify { fakeUserRepository.changeUser(user)}
            }
        }
    }
})

@Serializable
class ErrorResponse(val error: String)

fun setupForTesting(app: Application, testKoinModule: Module) {
    app.install(Koin) { modules(testKoinModule) }
    app.configureRouting()
    app.configureSerialization()
}
