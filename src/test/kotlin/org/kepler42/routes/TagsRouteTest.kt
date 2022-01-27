package org.kepler42.routes

import io.kotest.matchers.shouldBe
import io.ktor.application.*
import io.ktor.http.*
import io.mockk.*
import io.ktor.server.testing.*
import io.ktor.util.Identity.decode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kepler42.controllers.CommunityController
import org.kepler42.controllers.TagController
import org.kepler42.database.repositories.TagRepository
import org.kepler42.plugins.configureRouting
import org.kepler42.plugins.configureSerialization
import org.kepler42.models.Tag
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.kepler42.testUtils.*
import org.koin.dsl.module
import org.koin.ktor.ext.Koin




object TagsRouteTest : Spek({

    val fakeTagRepository = mockk<TagRepository>()
    fun setup(app: Application) {

        val testKoinModule = module {
            single {TagController(fakeTagRepository) }
        }
        app.install(Koin) {
            modules(testKoinModule)
        }
        app.configureRouting()
        app.configureSerialization()
    }

    describe("Tag route") {
        it("should get all tags") {
            withTestApplication({ setup(this) }) {
                val tagList = generateTags()
                every { fakeTagRepository.getAll() } answers { tagList }
                handleRequest(HttpMethod.Get, "/tags"){
                }.apply{
                    response.status() shouldBe HttpStatusCode.OK
                    val tagListResult = Json.decodeFromString<List<Tag>>(response.content!!)
                    tagListResult shouldBe tagList
                }
            }
        }
    }
})
