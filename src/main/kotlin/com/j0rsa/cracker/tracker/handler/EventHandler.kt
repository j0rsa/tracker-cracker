package com.j0rsa.cracker.tracker.handler

import com.j0rsa.cracker.tracker.Event
import com.j0rsa.cracker.tracker.Logging
import com.j0rsa.cracker.tracker.logger
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer

const val ACTIONS = "actions:get"

class EventHandler : Logging {
	private val logger = logger()

	fun EventHandlerSyntax.start() {
		ACTIONS.consume(::process)
	}

	private fun process(event: Event) {
		logger.debug("Processing $event")
	}
}

interface EventHandlerSyntax {
	val vertx: Vertx

	fun <T> String.consume(block: (T) -> Unit): MessageConsumer<T> =
		vertx.eventBus().consumer(this) {
			block(it.body())
		}
}

interface KafkaSyntax {
//	val consumer: KafkaConsumer<String, Event>
}