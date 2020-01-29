package com.j0rsa.bujo.tracker.model

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.*

object TagRepository {
    fun findAll(userId: UserId): List<Tag> {
        return Tag.wrapRows((UserTags leftJoin Tags)
            .slice(Tags.columns)
            .select { UserTags.userId eq userId.value }
            .orderBy(Tags.name to SortOrder.ASC))
            .toList()
    }

    fun findOne(tagName: String) = Tag.find { Tags.name eq tagName }.singleOrNull()

    fun findOneForUser(tagName: String, userId: UserId) = Tag.wrapRows((UserTags leftJoin Tags)
        .slice(Tags.columns)
        .select { (UserTags.userId eq userId.value) and (Tags.name eq tagName) })
        .singleOrNull()

    fun findOneByIdForUser(id: UUID, userId: UserId) = Tag.wrapRows((UserTags leftJoin Tags)
        .slice(Tags.columns)
        .select { (UserTags.userId eq userId.value) and (Tags.id eq id) })
        .singleOrNull()
}