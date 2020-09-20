package com.j0rsa.common

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import kotlin.time.seconds
import com.j0rsa.common.KafkaEventBus.send
import com.j0rsa.common.KafkaEventBus.listen
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object KafkaEventBus : EventBus {
	override val brokers: String = "localhost:9092"
	override val consumerGroup: String = "test"
}

object StringKafkaSyntax : KafkaSyntax<String> {
	override val serialize: (String) -> String = { it }
	override val deserialize: (String) -> String = { it }
}

@ExperimentalTime
class EventBusTest : StringSpec({

	"send a message" {
		StringKafkaSyntax.send("topic1", "test")
	}

	"receive a message" {
		val randomMessage = UUID.randomUUID().toString()
		val topic = "topic3"
		var received = false
		GlobalScope.launch {
			StringKafkaSyntax.listen(topic, true) {
				if (it == randomMessage) received = true
			}
		}
		StringKafkaSyntax.send(topic, randomMessage)

		eventually(2.seconds, 100.milliseconds) {
			received shouldBe true
		}
	}
})