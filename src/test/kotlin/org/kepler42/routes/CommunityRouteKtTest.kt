package org.kepler42.routes;

import com.google.api.Http
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.kepler42.controllers.CommunityRepository
import org.kepler42.models.Community
import kotlinx.serialization.json.Json
import org.kepler42.controllers.CommunityController
import org.kepler42.errors.UnauthorizedException
import org.kepler42.models.Contact
import org.kepler42.models.User
import org.kepler42.plugins.configureRouting
import org.kepler42.plugins.configureSerialization
import org.kepler42.utils.TokenValidator
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

fun generateCommunity(name: String, admin: String = "user-id", type: String = "open"): Community {
    return Community(
        id = name.hashCode(),
        name = name,
        slug = name.lowercase(),
        admin = admin,
        type = type,
        contacts = listOf(
            Contact(0, "Ada#7777", "ada@lovelace.com"),
            Contact(1, "Ada#7777", "ada@lovelace.com"),
            Contact(1, "Ada#7777", "ada@lovelace.com"),
        )
    )
}

fun generateCommunity(name: String): Community {
    return Community(
        id = name.hashCode(),
        name = name,
        slug = name.lowercase(),
        admin = "user-id",
        type = "open",
        contacts = emptyList()
    )
}

fun generateUser(name: String) : User {
    return User(
        id = name.hashCode().toString(),
        name = name,
        username = name.lowercase(),
        occupation = "dev",
        photoURL = "https://i.kym-cdn.com/entries/icons/original/000/017/108/faustooooooooooooooooooo.jpg",
        email = "user@gmail.com",
        contact = Contact(0, "Ada#7777", null),
    )
}

fun getAllCommunities(): List<Community> {
    return listOf(
        generateCommunity("Kotlin"),
        generateCommunity("C"),
        generateCommunity("Java"),
    )
}

fun getAllUsers(): List<User> {
    return listOf(
        generateUser("Ada"),
        generateUser("Roberto"),
        generateUser("Fausto"),
    )
}

@OptIn(ExperimentalSerializationApi::class)
object CommunityRouteTest: Spek({
    val fakeCommunityRepository: CommunityRepository by memoized { spyk() }
    val fakeTokenValidator = spyk<TokenValidator>()
    every { fakeTokenValidator.checkAuth(any()) } answers { "user-id" }

    fun setup(app: Application) {
        val testKoinModule = module {
            single { fakeCommunityRepository }
            single { fakeTokenValidator }
            single { CommunityController(get()) }
        }
        app.install(Koin) {
            modules(testKoinModule)
        }
        app.configureRouting()
        app.configureSerialization()
    }

    describe("Community route") {
        it("by searching communities without url query, it should find all communities") {
            withTestApplication({ setup(this) }) {
                val allCommunities = getAllCommunities()
                every { fakeCommunityRepository.fetchAllCommunities() } answers { allCommunities }

                handleRequest(HttpMethod.Get, "/communities").apply {
                    response.content shouldNotBe null
                    val results = Json.decodeFromString<List<Community>>(response.content!!)
                    results shouldBe allCommunities
                }
            }
        }

        it ("should find community by correct name") {
            withTestApplication ({ setup(this) }) {
                val javaCommunity = generateCommunity("Java")

                every { fakeCommunityRepository.fetchCommunitiesByName("Java")} answers  {
                    listOf(javaCommunity)
                }

                handleRequest(HttpMethod.Get, "/communities?name=Java").apply {
                    response.content shouldNotBe null
                    val results = Json.decodeFromString<List<Community>>(response.content!!)
                    results shouldBe listOf(javaCommunity)
                }
            }
        }

        it("should find community page by community id") {
            withTestApplication({ setup(this) }) {
                val kotlinCommunity = generateCommunity("Kotlin")
                val communityIdSlot = slot<Int>()
                val allCommunities = getAllCommunities()
                every { fakeCommunityRepository.fetchCommunity(capture(communityIdSlot)) } answers {
                    allCommunities.find { it.id == communityIdSlot.captured }
                }
                handleRequest(HttpMethod.Get, "/communities/${kotlinCommunity.id}").apply {
                    response.content shouldNotBe null
                    val community = Json.decodeFromString<Community>(response.content!!)
                    community shouldBeEqualToComparingFields kotlinCommunity
                }
            }
        }

        it("by searching communities with url query, it should find one community") {
            withTestApplication({ setup(this) }) {
                val kotlinCommunity = generateCommunity("Kotlin")
                every { fakeCommunityRepository.fetchCommunitiesByName("Kotlin") } answers {
                    listOf(kotlinCommunity)
                }
                handleRequest(HttpMethod.Get, "/communities?name=Kotlin").apply {
                    response.content shouldNotBe null
                    response.status() shouldBe HttpStatusCode.OK
                    val communities = Json.decodeFromString<List<Community>>(response.content!!)
                    communities shouldBe listOf(kotlinCommunity)
                }
            }
        }

        it("by posting a follower into a community, it should check the called functions") {
            withTestApplication({
                setup(this)
            }) {
                val ada = generateUser("Ada")
                every { fakeCommunityRepository.insertFollower(any(), any())} answers { nothing }
                every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                every { fakeCommunityRepository.checkAlreadyFollows(any(), 1)} answers { false }

                handleRequest(HttpMethod.Post, "/communities/1/followers") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(ada))
                }.apply {
                    response.content shouldNotBe null
                    response.status() shouldBe HttpStatusCode.OK
                }
                verify { fakeCommunityRepository.insertFollower(ada.id!!, 1)}
            }
        }
        it("by deleting a follower from a community, it should check the called functions") {
            withTestApplication({
                setup(this)
            }) {
                val community = generateCommunity("Kotlin")
                val follower = generateUser("Ada")
                every { fakeCommunityRepository.deleteFollower(any(), any()) } answers  { nothing }
                handleRequest(HttpMethod.Delete, "/communities/${community.id}/followers/${follower.id}") {
                    addHeader("Content-Type", "application/json")
                }.apply {
                    response.content shouldBe null
                    response.status() shouldBe HttpStatusCode.OK
                }
                verify { fakeCommunityRepository.deleteFollower( community.id, follower.id!!)}
            }
        }

        it("should get all followers from community") {
            withTestApplication({ setup(this) }){
                val community = generateCommunity("kotlin")
                val userList = getAllUsers()
                every { fakeCommunityRepository.fetchFollowers(community.id) } answers { userList }
                handleRequest(HttpMethod.Get, "/communities/${community.id}/followers") {
                    addHeader("Content-Type", "application/json")
                }.apply {
                    response.content shouldNotBe null
                    response.status() shouldBe HttpStatusCode.OK
                    val followers = Json.decodeFromString<List<User>>(response.content!!)
                    followers shouldBe userList
                }
            }
        }

        it("Should patch properties of a Community by its ID") {
            withTestApplication({
                setup(this)
            }) {
                val ada = generateUser("ada")
                val communityOld = generateCommunity("kotlin", ada.id!!, "closed")
                val communityNew = generateCommunity("kotlin", ada.id!!, "moderated")
                every { fakeCommunityRepository.fetchCommunity(communityOld.id) } answers { communityOld }
                every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                every { fakeCommunityRepository.updateCommunity(communityOld.id, communityNew) } answers { communityNew }
                handleRequest(HttpMethod.Patch, "/communities/${communityOld.id}") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(communityNew))
                }.apply{
                    response.content shouldNotBe null
                    response.status() shouldBe HttpStatusCode.OK
                    val communityPatched = Json.decodeFromString<Community>(response.content!!)
                    communityPatched shouldBe communityNew
                }
            }
        }

        it("should be able to create a community if doesn't yet exist") {
            withTestApplication({ setup(this) }) {
                val ada = generateUser("Ada")
                val community = generateCommunity("Kotlin", admin = ada.id!!)
                every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                every { fakeCommunityRepository.alreadyExists(any()) } answers { false }
                every { fakeCommunityRepository.insertCommunity(community) } answers { community }
                handleRequest(HttpMethod.Post, "/communities") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(community))
                }.apply {
                    response.content shouldNotBe null
                    response.status() shouldBe HttpStatusCode.OK
                    val createdCommunity = Json.decodeFromString<Community>(response.content!!)
                    createdCommunity shouldBe community
                }
            }
        }

        it("should return 401 when creating a community if user isn't authenticated") {
            withTestApplication({ setup(this) }) {
                val community = generateCommunity("Kotlin", admin = "1")
                every { fakeTokenValidator.checkAuth(any()) } throws UnauthorizedException()

                handleRequest(HttpMethod.Post, "/communities") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(community))
                }.apply {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                    verify { fakeCommunityRepository.insertCommunity(any()) wasNot Called }
                }
            }
        }
        xit("should return 401 if user tries to follow a community if not authenticated") {

        }

        xit("should return 401 if user tries to unfollow a community if not authenticated") {

        }

        xit("should return 401 if user tries to update a community if not authenticated") {

        }

        xit("should return 401 if user tries to update a community if not authenticated") {

        }

        xit("should not create a community if a community with the same name already exists") {

        }

        xit("should not create a community if it lacks a contact") {

        }
    }
})
