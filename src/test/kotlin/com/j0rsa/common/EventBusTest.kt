package com.j0rsa.common

import arrow.fx.extensions.io.concurrent.sleep
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import kotlin.time.seconds

object KafkaEventBus: EventBus {
    override val brokers: String = "localhost:9092"
    override val consumerGroup: String = "test"
}

object StringKafkaSyntax: KafkaSyntax<String> {
    override val serialize: (String) -> String = { it }
    override val deserialize: (String) -> String = { it }
}

@ExperimentalTime
class EventBusTest: StringSpec({

    "send a message" {
        with(KafkaEventBus) {
            StringKafkaSyntax.send("topic1", "test")
        }
    }

    "receive a message" {
        with(KafkaEventBus) {
            val randomMessage = UUID.randomUUID().toString()
            val topic = "topic3"
            var received = false
            Thread {
                StringKafkaSyntax.listen(topic, true) {
                    if (it == randomMessage) received = true
                }
            }.start()
            StringKafkaSyntax.send(topic, randomMessage)

            eventually(2.seconds, 100.milliseconds) {
                received shouldBe true
            }
        }
    }
})