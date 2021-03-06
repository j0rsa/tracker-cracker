package com.j0rsa.cracker.tracker.handler

import arrow.core.Either
import arrow.core.Right
import com.j0rsa.cracker.tracker.TrackerError
import com.j0rsa.cracker.tracker.blockingTx
import com.j0rsa.cracker.tracker.handler.RequestLens.tagIdPathLens
import com.j0rsa.cracker.tracker.handler.RequestLens.tagLens
import com.j0rsa.cracker.tracker.handler.RequestLens.userIdLens
import com.j0rsa.cracker.tracker.handler.ResponseState.OK
import com.j0rsa.cracker.tracker.model.TagId
import com.j0rsa.cracker.tracker.service.TagService
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext

object TagHandler {
	fun findAll(vertx: Vertx): suspend (RoutingContext) -> Either<TrackerError, Response<List<TagRow>>> = { req ->
		val tags = blockingTx(vertx) {
			TagService.findAll(userIdLens(req))
		}
		Right(Response(OK, tags))
	}

	fun update(vertx: Vertx): suspend (RoutingContext) -> Either<TrackerError, Response<TagRow>> = { req ->
		blockingTx(vertx) {
			TagService.update(userIdLens(req), Tag(tagIdPathLens(req), tagLens(req).name))
		}.map { Response(OK, it) }
	}
}

data class TagRequest(val name: String)
data class Tag(val id: TagId, val name: String)
typealias TagRow = Tag