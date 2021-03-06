package org.kepler42.routes;

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import org.kepler42.testUtils.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kepler42.database.repositories.CommunityRepository
import org.kepler42.database.repositories.UserRepository
import org.kepler42.errors.UnauthorizedException
import org.kepler42.models.*
import org.kepler42.plugins.configureRouting
import org.kepler42.plugins.configureSerialization
import org.kepler42.utils.TokenValidator
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@OptIn(ExperimentalSerializationApi::class)
object CommunityRouteTest: Spek({
    val fakeUserRepository by memoized {spyk<UserRepository>()}
    val fakeCommunityRepository by memoized {spyk<CommunityRepository>()}
    val fakeTokenValidator = spyk<TokenValidator>()
    every { fakeTokenValidator.checkAuth(any()) } answers { "user-id" }

    fun setup(app: Application) {
        val testKoinModule = module {
            single { fakeCommunityRepository }
            single { fakeUserRepository }
            single { fakeTokenValidator }
        }
        app.install(Koin) {
            modules(testKoinModule)
        }
        app.configureRouting()
        app.configureSerialization()
    }

    describe("Community route") {
        it("should get the 5 first communities in the database") {
            withTestApplication({ setup(this) }) {
                val allCommunities = getAllCommunities()
                every { fakeCommunityRepository.fetchAllCommunities(any()) } answers { allCommunities.subList(0, 5) }

                handleRequest(HttpMethod.Get, "/communities").apply {
                    response.content shouldNotBe null
                    val results = Json.decodeFromString<List<Community>>(response.content!!)
                    results shouldBe allCommunities.subList(0, 5)
                }
            }
        }

        it ("should find community by correct name") {
            withTestApplication ({ setup(this) }) {
                val javaCommunity = generateCommunity("Java")

                every { fakeCommunityRepository.search("Java", any())} answers  {
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
                every { fakeCommunityRepository.search("Kotlin", any()) } answers {
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

        it("should get community by slug") {
            withTestApplication ({ setup(this) }) {
                val community = generateCommunity("Python", 1)
                every { fakeCommunityRepository.fetchCommunityBySlug(slug = "python")} answers { community }
                handleRequest(HttpMethod.Get, "/communities?slug=${"python"}").apply {
                    response.status() shouldBe HttpStatusCode.OK
                    val communityResult = Json.decodeFromString<Community>(response.content!!)
                    communityResult shouldBe community
                }
            }
        }

        it("Should patch properties of a Community by its ID") {
            withTestApplication({ setup(this) }) {
                val ada = generateUser("ada")
                val communityOld = generateCommunity("kotlin", ada.id!!, CommunityType.MANAGED)
                val communityNew = generateCommunity("kotlin", ada.id!!, CommunityType.MODERATED)
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
                }
                verify { fakeCommunityRepository wasNot Called }
            }
        }
        it("should return 401 if user tries to update a community if not authenticated") {
            withTestApplication({ setup(this) }) {
                val ada = generateUser("Ada")
                val community = generateCommunity("kotlin", ada.id!!, CommunityType.MANAGED)
                val updatedCommunity = generateCommunity("kotlin", ada.id!!, CommunityType.MODERATED)
                every { fakeTokenValidator.checkAuth(any()) } throws UnauthorizedException()
                every { fakeCommunityRepository.fetchCommunity(community.id) } answers { community }
                every { fakeCommunityRepository.updateCommunity(community.id, community) } answers { updatedCommunity }

                handleRequest(HttpMethod.Patch, "/communities/${updatedCommunity.id}") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(community))
                }.apply {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                    verify { fakeCommunityRepository wasNot Called }
                }
            }
        }

        it("should not create a community if a community with the same name already exists") {
            withTestApplication({ setup(this)}) {
                val ada = generateUser("Ada")
                val community = generateCommunity("Kotlin", admin = ada.id!!)
                every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                every { fakeCommunityRepository.alreadyExists(community.name!!) } answers { true }

                handleRequest(HttpMethod.Post, "/communities") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(community))
                }.apply {
                    response.status() shouldBe HttpStatusCode.Conflict
                    verify(inverse = true) { fakeCommunityRepository.insertCommunity(community) }
                }
            }
        }

        it("should not create a community if it lacks a contact") {
            withTestApplication ({ setup(this) }){
                val user = generateUser("Ada")
                val community = generateCommunityWithoutContact("kotlin", user.id!!, CommunityType.OPEN)
                every { fakeTokenValidator.checkAuth(any()) } answers { user.id!! }
                handleRequest ( HttpMethod.Post, "/communities" ) {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(community))
                }.apply {
                    response.status() shouldBe HttpStatusCode.BadRequest
                }
            }
        }

        it("should not create a community if it has more than 3 contacts") {
            withTestApplication ({ setup(this) }) {
                val user = generateUser("Ada")
                val community = Community(
                    name = "Kotlin",
                    description = "kotlin",
                    admin = user.id!!,
                    type = CommunityType.OPEN,
                    contacts = listOf(
                        Contact(1, "a", "a"),
                        Contact(2, "b", "b"),
                        Contact(3, "c", "c"),
                        Contact(4, "d", "d"),
                    )
                )
                every { fakeTokenValidator.checkAuth(any()) } answers { user.id!! }

                handleRequest(HttpMethod.Post, "/communities") {
                    addHeader("Content-Type", "application/json")
                    setBody(Json.encodeToString(community))
                }.apply {
                    response.status() shouldBe HttpStatusCode.BadRequest
                    verify { fakeCommunityRepository wasNot Called }
                }
            }
        }

        it("should not create a community if it's type isn't recognized") {
            withTestApplication ({ setup(this) }) {
                val admin = generateUser("Ademir")
                every { fakeTokenValidator.checkAuth(any()) } answers { admin.id!! }

                handleRequest (HttpMethod.Post, "/communities" ) {
                    addHeader("Content-Type", "application/json")
                    setBody("""
                        {
                            "id":0,
                            "name":"Tipo Errado",
                            "description":"Tipo Errado",
                            "admin":"${admin.id!!}",
                            "contacts": [
                                {"title": "Discord", "link": "http://discord.gg"}
                            ],
                            "type":"Errado"
                        }
                    """.trimIndent())
                }.apply {
                    response.status() shouldBe HttpStatusCode.BadRequest
                }
            }
        }
        describe("followers route") {
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
                    verify(exactly = 1) { fakeCommunityRepository.insertFollower(ada.id!!, 1)}
                }
            }
            it("by deleting a follower from a community, it should check the called functions") {
                withTestApplication({
                    setup(this)
                }) {
                    val community = generateCommunity("Kotlin")
                    val follower = generateUser("Ada")
                    every { fakeTokenValidator.checkAuth(any()) } answers { follower.id!! }
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

            it("should return 401 if user tries to follow a community if not authenticated") {
                withTestApplication({ setup(this) }) {
                    val community = generateCommunity("Kotlin", admin = "1")
                    every { fakeTokenValidator.checkAuth(any()) } throws UnauthorizedException()

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/followers") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(community))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                    verify { fakeCommunityRepository wasNot Called}
                }
            }

            it("should return 401 if user tries to unfollow a community if not authenticated") {
                withTestApplication({ setup(this) }) {
                    val user = generateUser("Fausto")
                    val community = generateCommunity("Kotlin", admin = user.id!!)
                    every { fakeTokenValidator.checkAuth(any()) } throws UnauthorizedException()

                    handleRequest(HttpMethod.Delete, "/communities/${community.id}/followers/${user.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(community))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                        verify { fakeCommunityRepository wasNot Called }
                    }
                }
            }


        }

        describe("tags route") {
            it("should filter a community list by an specific tag") {
                withTestApplication ({ setup(this) }) {
                    val communityList = listOf(
                        generateCommunity("Kotlin", 1),
                        generateCommunity("Java", 1)
                    )
                    val filterTags = listOf(1)
                    val tagString = filterTags.joinToString(",")
                    every { fakeCommunityRepository.search(name = null, tags = filterTags) } answers { communityList }
                    handleRequest(HttpMethod.Get, "/communities?tags=${tagString}") {
                    }.apply {
                        response.status() shouldBe HttpStatusCode.OK
                        val communityListResult = Json.decodeFromString<List<Community>>(response.content!!)
                        communityListResult shouldBe communityList
                    }
                }

            }

            it("should filter a community list by name and an specific tag") {
                withTestApplication ({ setup(this) }) {
                    val filterTags = listOf(1)
                    val name = "Java"
                    val expectedList = listOf(
                        generateCommunity("Java", 1),
                        generateCommunity("Javascript", 1),
                        generateCommunity("Javascript", 1),
                    )
                    every { fakeCommunityRepository.search(name = name, tags = filterTags) } answers { expectedList }
                    val tagString = filterTags.joinToString(",")
                    handleRequest(HttpMethod.Get, "/communities?tags=${tagString}&name=${name}").apply {
                        response.status() shouldBe HttpStatusCode.OK
                        val communityListResult = Json.decodeFromString<List<Community>>(response.content!!)
                        communityListResult shouldBe expectedList
                    }
                }

            }

            it("should filter a community list from multiple tags") {
                withTestApplication ({ setup(this) }) {
                    val filterTags = listOf(1, 3)
                    val expectedList = listOf(
                        generateCommunity("Java", 1),
                        generateCommunity("Javascript", 1),
                        generateCommunity("DotaJava", 3),
                    )
                    every { fakeCommunityRepository.search(name = null, tags = filterTags) } answers { expectedList }
                    val tagString = filterTags.joinToString(",")
                    handleRequest(HttpMethod.Get, "/communities?tags=${tagString}").apply {
                        response.status() shouldBe HttpStatusCode.OK
                        val communityListResult = Json.decodeFromString<List<Community>>(response.content!!)
                        communityListResult shouldBe expectedList
                    }
                }

            }

            it("should return an empty list if searching a community name that doesnt exist even with tags") {
                withTestApplication ({ setup(this) }) {
                    val filterTags = listOf(1, 3)
                    val name = "Non Java"
                    every { fakeCommunityRepository.search(name = name, tags = filterTags) } answers { emptyList() }
                    val tagString = filterTags.joinToString(",")
                    handleRequest(HttpMethod.Get, "/communities?tags=${tagString}").apply {
                        response.status() shouldBe HttpStatusCode.OK
                        val communityListResult = Json.decodeFromString<List<Community>>(response.content!!)
                        communityListResult shouldBe emptyList()
                    }
                }
            }
        }

        describe("moderators route") {
            it("should get a list of a community's moderators") {
                withTestApplication ({ setup(this) }) {
                    val community = generateCommunity("Kotlin")
                    val moderators = listOf(
                        generateUser("Ada"),
                        generateUser("Roberto"),
                    )
                    every { fakeCommunityRepository.fetchModerators(any()) } answers { moderators }

                    handleRequest(HttpMethod.Get, "/communities/${community.id}/moderators").apply {
                        response.status() shouldBe HttpStatusCode.OK
                        val result = Json.decodeFromString<List<User>>(response.content!!)
                        result shouldBe moderators
                    }
                }
            }

            it("should add a moderator to a community if requester is admin") {
                withTestApplication ({ setup(this) }) {
                    val admin = generateUser("Admin")
                    val moderator = generateUser("Ada")
                    val community = generateCommunity("Kotlin", admin = admin.id!!)
                    every { fakeTokenValidator.checkAuth(any()) } answers { admin.id!! }
                    every { fakeUserRepository.getUserById(moderator.id!!) } answers { moderator }
                    every { fakeCommunityRepository.insertModerator(community.id, moderator.id!!) } just Runs
                    every { fakeCommunityRepository.fetchCommunity(community.id) } answers { community }

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/moderators") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(moderator))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.OK
                        verify { fakeCommunityRepository.insertModerator(community.id, moderator.id!!) }
                    }
                }
            }

            it("should delete a moderator of a community if requester is admin") {
                withTestApplication ({ setup(this) }) {
                    val admin = generateUser("Admin")
                    val moderator = generateUser("Ada")
                    val community = generateCommunity("Kotlin", admin = admin.id!!)
                    every { fakeTokenValidator.checkAuth(any()) } answers { admin.id!! }
                    every { fakeCommunityRepository.fetchCommunity(community.id) } answers { community }
                    every { fakeUserRepository.getUserById(moderator.id!!) } answers { moderator }

                    handleRequest(HttpMethod.Delete, "/communities/${community.id}/moderators/${moderator.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(moderator))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.OK
                        verify { fakeCommunityRepository.deleteModerator(community.id, moderator.id!!) }
                    }
                }
            }

            it("should not be able to add moderators if requester is not admin") {
                withTestApplication ({ setup(this) }) {
                    val moderator = generateUser("Ada")
                    val actualAdmin = generateUser("Admin")
                    val community = generateCommunity("Kotlin", admin = actualAdmin.id!!)

                    val loggedUser = generateUser("Not admin")
                    every { fakeTokenValidator.checkAuth(any()) } answers { loggedUser.id!! }
                    every { fakeCommunityRepository.fetchCommunity(community.id) } answers { community }

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/moderators") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(moderator))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Forbidden
                        verify(inverse = true) { fakeCommunityRepository.insertCommunity(any()) }
                    }
                }
            }

            it("should not add the same moderator twice") {
                withTestApplication ({ setup(this) }) {
                    val moderator = generateUser("Ada")
                    val admin = generateUser("Admin")
                    val community = generateCommunity("Kotlin", admin = admin.id!!)

                    every { fakeTokenValidator.checkAuth(any()) } answers { admin.id!! }
                    every { fakeCommunityRepository.fetchCommunity(community.id) } answers { community }
                    every { fakeCommunityRepository.fetchModerators(community.id) } answers { listOf(moderator) }
                    every { fakeUserRepository.getUserById(moderator.id!!) } answers { moderator }

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/moderators") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(moderator))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.BadRequest
                        verify(inverse = true) { fakeCommunityRepository.insertModerator(any(), any()) }
                    }
                }
            }

            it("should not be able to remove a moderator if not admin") {
                withTestApplication({ setup(this) }) {
                    val actualAdmin = generateUser("Admin")
                    val moderator = generateUser("Ada")
                    val community = generateCommunity("Kotlin", admin = actualAdmin.id!!)
                    val loggedUser = generateUser("Not admin")
                    every { fakeTokenValidator.checkAuth(any()) } answers { loggedUser.id!! }
                    every { fakeCommunityRepository.fetchCommunity(community.id) } answers { community }
                    every { fakeUserRepository.getUserById(moderator.id!!) } answers { moderator }

                    handleRequest(HttpMethod.Delete, "/communities/${community.id}/moderators/${moderator.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(moderator))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Forbidden
                        verify(inverse = true) { fakeCommunityRepository.deleteModerator(any(), any()) }
                    }
                }
            }
        }

        describe("contacts route") {
            it("should return 401 when creating a contact if user isn't authenticated") {
                withTestApplication({ setup(this) }) {
                    val community = generateCommunity("Kotlin", admin = "1")
                    every { fakeTokenValidator.checkAuth(any()) } throws UnauthorizedException()

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/contacts") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(community.contacts))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                    verify { fakeCommunityRepository wasNot Called }
                }
            }

            it("should return 401 when creating a contact if user isn't community admin") {
                withTestApplication({ setup(this) }) {
                    val ada = generateUser("Ada")
                    val community = generateCommunity("kotlin")
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                    every { fakeCommunityRepository.fetchCommunity(any()) } answers { community }

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/contacts") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(community.contacts))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                    verify(inverse = true) { fakeCommunityRepository.insertContacts(any(), any()) }
                }
            }

            it("should not create a contact if the community does not exist") {
                withTestApplication({
                    setup(this)
                }) {
                    val ada = generateUser("Ada")
                    val community = generateCommunity("kotlin", ada.id!!, CommunityType.OPEN)
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/contacts") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(community.contacts))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.NotFound
                    }
                    verify(inverse = true) { fakeCommunityRepository.insertContacts(community.contacts, community.id) }
                }
            }

            it("should not create a contact if the title does not exist") {
                withTestApplication({
                    setup(this)
                }) {
                    val ada = generateUser("Ada")
                    val community = Community(
                        name = "Kotlin",
                        description = "kotlin",
                        admin = ada.id!!,
                        slug = "kotlin",
                        photoURL = "troll",
                        type = CommunityType.OPEN,
                        contacts = listOf(
                            Contact(1, "a", "a"),
                        )
                    )
                    val contacts = listOf(
                        Contact(5, "a", "a"),
                        Contact(6, "", "b"),
                    )
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                    every { fakeCommunityRepository.fetchCommunity(any()) } answers { community }

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/contacts") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contacts))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.BadRequest
                    }
                    verify(inverse = true) { fakeCommunityRepository.insertContacts(any(), any()) }
                }
            }

            it("should not create a contact if there are already 3 contacts") {
                withTestApplication({
                    setup(this)
                }) {
                    val ada = generateUser("Ada")
                    val community = Community(
                        name = "Kotlin",
                        description = "kotlin",
                        admin = ada.id!!,
                        type = CommunityType.OPEN,
                        contacts = listOf(
                            Contact(1, "a", "a"),
                            Contact(2, "b", "b"),
                            Contact(5, "c", "c"),
                        )
                    )
                    val contacts = listOf(
                        Contact(6, "aa", "b"),
                    )
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                    every { fakeCommunityRepository.fetchCommunity(any()) } answers { community }

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/contacts") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contacts))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.BadRequest
                    }
                    verify(inverse = true) { fakeCommunityRepository.insertContacts(community.contacts, community.id) }
                }
            }

            it("by posting a contact into a community, it should check the called functions") {
                withTestApplication({
                    setup(this)
                }) {
                    val ada = generateUser("Ada")
                    val community = Community(
                        name = "Kotlin",
                        description = "kotlin",
                        admin = ada.id!!,
                        type = CommunityType.OPEN,
                        contacts = listOf(
                            Contact(1, "a", "a"),
                            Contact(5, "a", "a"),
                        )
                    )
                    val contacts = listOf(
                        Contact(6, "k", "b"),
                    )
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                    every { fakeCommunityRepository.fetchCommunity(any()) } answers { community }
                    every { fakeCommunityRepository.insertContacts(any(), any()) } answers { contacts }

                    handleRequest(HttpMethod.Post, "/communities/${community.id}/contacts") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contacts))
                    }.apply {
                        response.content shouldNotBe null
                        response.status() shouldBe HttpStatusCode.OK
                        val results = Json.decodeFromString<List<Contact>>(response.content!!)
                        results shouldBe contacts
                    }
                    verify(exactly = 1) { fakeCommunityRepository.insertContacts(any(), any()) }
                }
            }

            it("should return 401 when updating a contact if user isn't authenticated") {
                withTestApplication({ setup(this) }) {
                    val contact = Contact(1, "lol", "a")
                    every { fakeTokenValidator.checkAuth(any()) } throws UnauthorizedException()

                    handleRequest(HttpMethod.Patch, "/communities/0/contacts/${contact.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contact))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                    verify { fakeCommunityRepository wasNot Called }
                }
            }

            it("should return 401 when updating a contact if user isn't community admin") {
                withTestApplication({ setup(this) }) {
                    val ada = generateUser("Ada")
                    val community = generateCommunity("kotlin")
                    val contact = Contact(1, "discord", "a")
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                    every { fakeCommunityRepository.fetchCommunity(any()) } answers { community }

                    handleRequest(HttpMethod.Patch, "/communities/${community.id}/contacts/${contact.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contact))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                    verify(inverse = true) { fakeCommunityRepository.updateContact(any(), any()) }
                }
            }

            it("should not update a contact if the community does not exist") {
                withTestApplication({
                    setup(this)
                }) {
                    val ada = generateUser("Ada")
                    val contact = Contact(1, "a", "a")
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }

                    handleRequest(HttpMethod.Patch, "/communities/1/contacts/${contact.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contact))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.NotFound
                    }
                    verify(inverse = true) { fakeCommunityRepository.updateContact(any(), any()) }
                }
            }

            it("should not update a contact if the title does not exist") {
                withTestApplication({
                    setup(this)
                }) {
                    val ada = generateUser("Ada")
                    val community = generateCommunity("Kotlin", ada.id!!, CommunityType.OPEN)
                    val contact = Contact(5, "", "a")
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                    every { fakeCommunityRepository.fetchCommunity(any()) } answers { community }

                    handleRequest(HttpMethod.Patch, "/communities/${community.id}/contacts/${contact.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contact))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.BadRequest
                    }
                    verify(inverse = true) { fakeCommunityRepository.updateContact(any(), any()) }
                }
            }

            it("Should patch properties of a Contact by its ID") {
                withTestApplication({
                    setup(this)
                }) {
                    val ada = generateUser("Ada")
                    val community = generateCommunity("Kotlin", ada.id!!, CommunityType.OPEN)
                    val contact = Contact(6, "k", "b")
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                    every { fakeCommunityRepository.fetchCommunity(any()) } answers { community }
                    every { fakeCommunityRepository.updateContact(any(), any()) } answers { contact }

                    handleRequest(HttpMethod.Patch, "/communities/${community.id}/contacts/${contact.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contact))
                    }.apply {
                        response.content shouldNotBe null
                        response.status() shouldBe HttpStatusCode.OK
                        val results = Json.decodeFromString<Contact>(response.content!!)
                        results shouldBe contact
                    }
                    verify(exactly = 1) { fakeCommunityRepository.updateContact(any(), any()) }
                }
            }

            it("should return 401 when deleting a contact if user isn't authenticated") {
                withTestApplication({ setup(this) }) {
                    val contact = Contact(1, "discord", "a")
                    every { fakeTokenValidator.checkAuth(any()) } throws UnauthorizedException()

                    handleRequest(HttpMethod.Delete, "/communities/1/contacts/${contact.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contact))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                    verify { fakeCommunityRepository wasNot Called }
                }
            }

            it("should return 401 when deleting a contact if user isn't community admin") {
                withTestApplication({ setup(this) }) {
                    val ada = generateUser("Ada")
                    val community = generateCommunity("kotlin")
                    val contact = Contact(1, "discord", "a")
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                    every { fakeCommunityRepository.fetchCommunity(any()) } answers { community }

                    handleRequest(HttpMethod.Delete, "/communities/${community.id}/contacts/${contact.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contact))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                    verify(inverse = true) { fakeCommunityRepository.deleteContact(any(), any()) }
                }
            }

            it("should not delete a contact if the community does not exist") {
                withTestApplication({
                    setup(this)
                }) {
                    val ada = generateUser("Ada")
                    val contact = Contact(1, "a", "a")
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }

                    handleRequest(HttpMethod.Delete, "/communities/1/contacts/${contact.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contact))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.NotFound
                    }
                    verify(inverse = true) { fakeCommunityRepository.deleteContact(any(), any()) }
                }
            }

            it("by deleting a contact from a community, it should check the functions called") {
                withTestApplication({
                    setup(this)
                }) {
                    val ada = generateUser("Ada")
                    val community = generateCommunity("Kotlin", ada.id!!, CommunityType.OPEN)
                    val contact = Contact(6, "k", "b")
                    every { fakeTokenValidator.checkAuth(any()) } answers { ada.id!! }
                    every { fakeCommunityRepository.fetchCommunity(any()) } answers { community }
                    every { fakeCommunityRepository.deleteContact(any(), any()) } answers { contact }

                    handleRequest(HttpMethod.Delete, "/communities/${community.id}/contacts/${contact.id}") {
                        addHeader("Content-Type", "application/json")
                        setBody(Json.encodeToString(contact))
                    }.apply {
                        response.content shouldBe null
                        response.status() shouldBe HttpStatusCode.OK
                    }
                    verify(exactly = 1) { fakeCommunityRepository.deleteContact(any(), any()) }
                }
            }
        }
    }
})
