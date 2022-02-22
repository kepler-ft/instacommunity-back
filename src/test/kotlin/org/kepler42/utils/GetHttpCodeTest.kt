package org.kepler42.utils

import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.http.content.*
import org.kepler42.errors.AlreadyRelatedException
import org.kepler42.errors.ResourceNotFoundException
import org.kepler42.errors.UnknownErrorException
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object GetHttpCodeTest: Spek({

    describe("function getHttpCode") {
        it("returns 500 when unknown error is sent") {
            val unknownError = UnknownErrorException()
            val code = getHttpCode(unknownError)
            code shouldBe HttpStatusCode.InternalServerError
        }

        it("Returns 400 when alreadyRelatedException is sent") {
            val exception = AlreadyRelatedException()
            val code = getHttpCode(exception)
            code shouldBe HttpStatusCode.BadRequest
        }

        it("Returns 404 when resource is not found") {
            val exception = ResourceNotFoundException()
            val code = getHttpCode(exception)
            code shouldBe HttpStatusCode.NotFound
        }
    }
})
