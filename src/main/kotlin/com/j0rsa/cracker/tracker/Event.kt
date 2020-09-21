package com.j0rsa.cracker.tracker

import com.j0rsa.cracker.tracker.model.ActionId
import com.j0rsa.cracker.tracker.model.HabitId
import com.j0rsa.cracker.tracker.model.Period
import com.j0rsa.cracker.tracker.model.UserId
import org.joda.time.LocalDateTime
import kotlin.reflect.KClass

sealed class Event(val type: EventTypes)

data class TagActionCreated(
	val actionId: ActionId,
	val userId: UserId,
	val tags: Set<String>,
	val date: LocalDateTime,
	val message: String?,
) : Event(EventTypes.TAG_ACTION_CREATED)

data class HabitCreated(
	val habitId: HabitId,
	val userId: UserId,
	val tags: Set<String>,
	val numberOfRepetitions: Int,
	val period: Period,
	val message: String? = null,
) : Event(EventTypes.HABIT_CREATED)

enum class EventTypes(val kClass: KClass<out Event>) {
	TAG_ACTION_CREATED(TagActionCreated::class),
	HABIT_CREATED(HabitCreated::class);

	companion object {
		fun findValue(name: String): EventTypes? = values().find { it.name == name }
	}
}