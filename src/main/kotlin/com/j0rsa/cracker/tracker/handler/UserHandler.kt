package com.j0rsa.cracker.tracker.handler

import arrow.core.Either
import arrow.core.Right
import com.j0rsa.cracker.tracker.TrackerError
import com.j0rsa.cracker.tracker.blockingTx
import com.j0rsa.cracker.tracker.handler.RequestLens.telegramUserIdLens
import com.j0rsa.cracker.tracker.handler.RequestLens.telegramUserLens
import com.j0rsa.cracker.tracker.model.UserId
import com.j0rsa.cracker.tracker.service.UserService
import com.j0rsa.cracker.tracker.handler.ResponseState.*
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext

object UserHandler {
	fun createOrUpdateUser(vertx: Vertx): suspend (RoutingContext) -> Either<TrackerError, Response<UserId>> = { req ->
		val user = telegramUserLens(req)
		Right(blockingTx(vertx) {
			UserService.findOneBy(user.telegramId)
				?.let(updated(user))
				?: create(user)
		})
	}

	fun findUser(vertx: Vertx): suspend (RoutingContext) -> Either<TrackerError, Response<UserInfo>> = { req ->
		blockingTx(vertx) {
			UserService.findOne(telegramUserIdLens(req))
		}.map { Response(OK, it) }
	}

	private fun updated(userInfo: User) = { user: com.j0rsa.cracker.tracker.model.User ->
		val idValue = UserService.updateUser(userInfo)(user).idValue()
		Response(OK, idValue)
	}

	private fun create(userInfo: User): Response<UserId> {
		val idValue = UserService.createUser(userInfo).idValue()
		return Response(CREATED, idValue)
	}
}

data class User(
	val id: UserId? = null,
	val telegramId: Long,
	val firstName: String = "",
	val lastName: String = "",
	val language: String = ""
)

typealias UserInfo = User