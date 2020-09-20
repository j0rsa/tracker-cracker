package com.j0rsa.cracker.tracker

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

data class AppConfig(
	val db: DbConfig,
	val swagger: SwaggerConf,
	val port: Int,
	val specPath: String,
	val redis: RedisConf,
	val kafka: KafkaConfig,
)

data class DbConfig(
	val url: String,
	val driver: String,
	val user: String,
	val password: String,
	val maxPool: Int
)

data class KafkaConfig(
	val brokers: String,
)

data class SwaggerConf(
	val enabled: Boolean,
	val port: Int
)

data class RedisConf(
	val host: String,
)

object Config {
	val app: AppConfig = ConfigFactory.load().extract("app")
}

