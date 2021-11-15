package org.kepler42.routes

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*

import org.kepler42.models.*
import org.kepler42.database.operations.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Route.communityRoute() {
	    route("/communities") {
			get ("{id}") {
				val communityId = call.parameters["id"] 
				val community: Community? = fetchCommunity(communityId!!.toInt())
				if (community == null) {
					call.respond(HttpStatusCode.NotFound)
				} else {
					call.respond(community)
				} 
			}

        	post ("{id}/followers") {
				try {
					val user = call.receive<User>()
					val communityId = call.parameters["id"]

					if (!checkAlreadyFollows(user.id, communityId!!.toInt())) {
						val response: UserCommunity = insertFollower(UserCommunity(
							userId = user.id,
							communityId = communityId.toInt()))
						call.respond(response)
					} else {
						println ("já existe")
						call.respond(HttpStatusCode.BadRequest, mapOf("error" to "User already follows this community"))
					}
				} catch (e: ExposedSQLException) {
					call.respond(HttpStatusCode.InternalServerError,
					mapOf("error" to "Something has gone pretty bad"))
				}
			}

			get ("{id}/followers") {
				val communityId = call.parameters["id"]
				val followers = fetchFollowers(communityId!!.toInt())
				if (followers == null) {
					call.respond<List<User>>(emptyList())
				} else {
					call.respond(followers)
				}
			}
	}
}
