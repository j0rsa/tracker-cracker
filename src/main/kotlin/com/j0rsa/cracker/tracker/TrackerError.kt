package com.j0rsa.cracker.tracker

sealed class TrackerError {
	object NotFound : TrackerError()
	data class SyStemError(val message: String) : TrackerError()
}

typealias NotFound = TrackerError.NotFound
typealias SystemError = TrackerError.SyStemError

data class BadRequestError(
	val error: String,
	val details: Any?
)