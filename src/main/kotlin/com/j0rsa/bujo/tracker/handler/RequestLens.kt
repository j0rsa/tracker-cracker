package com.j0rsa.bujo.tracker.handler

import arrow.core.Either
import com.j0rsa.bujo.tracker.TrackerError
import com.j0rsa.bujo.tracker.TrackerJackson.auto
import com.j0rsa.bujo.tracker.model.ActionId
import com.j0rsa.bujo.tracker.model.HabitId
import com.j0rsa.bujo.tracker.model.UserId
import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Header
import org.http4k.lens.Path
import org.http4k.lens.uuid

object RequestLens {
    val habitInfoLens = Body.auto<HabitInfoView>().toLens()
    val habitLens = Body.auto<Habit>().toLens()
    val multipleHabitsLens = Body.auto<List<HabitsInfoView>>().toLens()
    val habitIdLens = Path.uuid().map(::HabitId).of("id")

    val userIdLens = Header.uuid().map(::UserId).required("X-Auth-Id")
    val userIdResponseLens = Body.auto<UserId>().toLens()
    val telegramUserLens = Body.auto<User>().toLens()

    val tagLens = Body.auto<Tag>().toLens()
    val tagsLens = Body.auto<List<Tag>>().toLens()
    val actionLens = Body.auto<ActionView>().toLens()
    val multipleActionLens = Body.auto<List<ActionView>>().toLens()
    val actionIdLens = Path.uuid().map(::ActionId).of("id")

    fun response(result: Either.Left<TrackerError>): Response = when (result.a) {
        TrackerError.NotFound -> Response(Status.NOT_FOUND)
        is TrackerError.SyStemError -> Response(Status.INTERNAL_SERVER_ERROR)
    }
}