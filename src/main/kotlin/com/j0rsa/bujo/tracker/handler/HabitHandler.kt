package com.j0rsa.bujo.tracker.handler

import arrow.core.Either
import com.j0rsa.bujo.tracker.TrackerError
import com.j0rsa.bujo.tracker.TransactionManager
import com.j0rsa.bujo.tracker.handler.RequestLens.habitIdLens
import com.j0rsa.bujo.tracker.handler.RequestLens.habitLens
import com.j0rsa.bujo.tracker.handler.RequestLens.multipleHabitsLens
import com.j0rsa.bujo.tracker.handler.RequestLens.response
import com.j0rsa.bujo.tracker.handler.RequestLens.userLens
import com.j0rsa.bujo.tracker.model.HabitId
import com.j0rsa.bujo.tracker.model.HabitRow
import com.j0rsa.bujo.tracker.model.HabitService
import com.j0rsa.bujo.tracker.model.TagRow
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import java.util.*

object HabitHandler {

    fun create() = { req: Request ->
        val habitId = TransactionManager.tx { HabitService.create(req.toHabitDto()) }
        Response(CREATED).body(habitId.toString())
    }

    fun delete(): (Request) -> Response = { req: Request ->
        val result = TransactionManager.tx {
            HabitService.deleteOne(habitIdLens(req), userLens(req))
        }
        when (result) {
            is Either.Left -> response(result)
            is Either.Right -> Response(NO_CONTENT)
        }
    }

    fun findOne() = { req: Request ->
        val habitResult = TransactionManager.tx {
            HabitService.findOneBy(habitIdLens(req), userLens(req))
        }
        responseFrom(habitResult)
    }

    fun findAll() = { req: Request ->
        val habits = TransactionManager.tx {
            HabitService.findAll(userLens(req))
        }.map { it.toView() }
        multipleHabitsLens(habits, Response(OK))
    }

    fun update() = { req: Request ->
        val habitResult = TransactionManager.tx {
            HabitService.update(req.toHabitDto())
        }
        responseFrom(habitResult)
    }

    private fun responseFrom(habitResult: Either<TrackerError, HabitRow>): Response = when (habitResult) {
        is Either.Left -> response(habitResult)
        is Either.Right -> habitLens(habitResult.b.toView(), Response(OK))
    }

    private fun Request.toHabitDto() = HabitRow(habitLens(this), userLens(this))
}

data class HabitView(
    val name: String,
    val quote: String?,
    val bad: Boolean?,
    val tagList: List<TagRow>,
    val id: HabitId? = null
)