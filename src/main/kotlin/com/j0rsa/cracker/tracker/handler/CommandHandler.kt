package com.j0rsa.cracker.tracker.handler

import arrow.core.Either
import arrow.core.right
import com.j0rsa.common.EventBus
import com.j0rsa.common.KafkaSyntax
import com.j0rsa.cracker.tracker.*

object CommandHandler : Logging {
	val logger = logger()

	inline fun <reified E : Event> EventSyntax.process(command: Command): Either<TrackerError, E> = execute(command)

	inline fun <reified E : Event> EventSyntax.execute(command: Command): Either<TrackerError, E> = run {
		logger.debug("Processing $command")
		val result = when (command) {
			is CreateTagAction -> command.toEvent()
			is CreateHabit -> command.toEvent()
		} as E
		result.publish()
		result.right()
	}

	fun CreateTagAction.toEvent(): TagActionCreated = TagActionCreated(actionId, userId, tags, date, message)
	fun CreateHabit.toEvent(): HabitCreated =
		HabitCreated(habitId, userId, tags, numberOfRepetitions, period, message)
}

interface EventSyntax : KafkaSyntax<Event>, EventBus {
	fun Event.publish(): Unit = send(Config.app.kafka.eventTopic, this)

	companion object {

		operator fun invoke(bus: EventBus): EventSyntax =
			object : EventSyntax, EventBus by bus {
				override val serialize: (Event) -> String = Serializer::toJson
				override val deserialize: (String) -> Event = Serializer::fromJson
			}
	}
}