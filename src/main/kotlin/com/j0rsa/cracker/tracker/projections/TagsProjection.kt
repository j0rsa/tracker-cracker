package com.j0rsa.cracker.tracker.projections

import com.j0rsa.cracker.tracker.TagActionCreated
import com.j0rsa.cracker.tracker.Event
import com.j0rsa.cracker.tracker.HabitCreated
import com.j0rsa.cracker.tracker.service.CacheService
import io.vertx.core.Vertx
import org.apache.kafka.clients.consumer.KafkaConsumer

class TagsProjection(val vertx: Vertx) {
	private val config = mapOf(
		"bootstrap.servers" to "localhost:9092",
		"key.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
		"value.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
		"group.id" to "my_group",
	)
	fun start() {
		//TODO: replace with event bus (and kafka under kafka bridge)
//		consumer.handler { record ->
//			when (val event = record.value()) {
//				is HabitCreated -> CacheService.sadd(listOf(event)) {
//					it.userId.value.toString() to it.tags.toSet()
//				}
//				is TagActionCreated -> CacheService.sadd(listOf(event)) {
//					it.userId.value.toString() to it.tags.toSet()
//				}
//				else -> {
//				}
//			}
//		}
//		consumer.subscribe("events") {
//			if (it.succeeded()) {
//				println("Subscribed")
//			}
//		}

	}
}