package com.j0rsa.common

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.*
import kotlin.time.seconds

interface Broker {
    val brokers: String
    val consumerGroup: String
}

interface KafkaSyntax<T> {
    val serialize: (T) -> String
    val deserialize: (String) -> T
}

interface EventBus : Broker {
    private fun consumerProps() =
        Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
            put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        }

    private fun producerProps() =
        Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        }

    fun <T> KafkaSyntax<T>.listen(topic: String, fromBeginning: Boolean = false,  handle: (T) -> Unit) {
        val consumer = KafkaConsumer<Long, String>(consumerProps().apply {
            if (fromBeginning) put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }).apply {
            subscribe(setOf(topic))
        }
        while (true) {
            consumer.poll(Duration.ofSeconds(1)).forEach {
                handle(deserialize(it.value()))
            }
        }
    }

    fun <T> KafkaSyntax<T>.send(topic: String, obj: T) {
        val producer = KafkaProducer<Long, String>(producerProps())
        producer.send(ProducerRecord(topic, serialize(obj))).get()
    }
}