package com.j0rsa.cracker.tracker

import com.j0rsa.common.EventBus

object AppEventBus : EventBus {
	override val brokers: String = Config.app.kafka.brokers
	override val consumerGroup: String = Config.app.kafka.consumerGroup
}