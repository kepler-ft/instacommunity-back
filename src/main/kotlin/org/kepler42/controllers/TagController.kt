package org.kepler42.controllers

import org.kepler42.models.*
import org.kepler42.database.repositories.TagRepository
import org.kepler42.database.entities.TagEntity


class TagController(private val tagRepository: TagRepository) {
    fun getAll(): List<Tag> {
        return tagRepository.getAll()
    }
}
