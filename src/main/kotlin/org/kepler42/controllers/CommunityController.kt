package org.kepler42.controllers

import org.kepler42.models.*
import org.kepler42.database.repositories.CommunityRepository
import org.kepler42.errors.*

class CommunityController(private val communityRepository: CommunityRepository) {
    private fun nameIsValid(name: String) =
            if (name.isEmpty()) false else name.length < 200

    fun getBySlug(slug: String):Community {
        return communityRepository.fetchCommunityBySlug(slug)
            ?: throw ResourceNotFoundException("Community Not Found")
    }

    fun search(name: String?, tagId: List<Int>?): List<Community> {
        return communityRepository.search(name, tagId) ?: emptyList()
    }
}
