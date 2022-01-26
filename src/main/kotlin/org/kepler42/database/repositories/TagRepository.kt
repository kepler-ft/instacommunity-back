package org.kepler42.database.repositories
import org.jetbrains.exposed.sql.transactions.transaction
import org.kepler42.database.entities.TagEntity
import org.kepler42.models.Tag

interface TagRepository {
    fun getAll(): List<Tag>
}

class TagRepositoryImpl: TagRepository {
    override fun getAll(): List<Tag> {
        return transaction {
            TagEntity.all().map { it.toModel() }
        }
    }
}
