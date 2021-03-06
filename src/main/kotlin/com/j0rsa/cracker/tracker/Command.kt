package com.j0rsa.cracker.tracker

import com.j0rsa.cracker.tracker.model.ActionId
import com.j0rsa.cracker.tracker.model.HabitId
import com.j0rsa.cracker.tracker.model.Period
import com.j0rsa.cracker.tracker.model.UserId
import org.joda.time.LocalDateTime

sealed class Command

data class CreateTagAction(
	val userId: UserId,
	val tags: Set<String>,
	val date: LocalDateTime,
	val message: String?,
	val actionId: ActionId = ActionId.randomValue(),
) : Command()

data class CreateHabit(
	val habitId: HabitId = HabitId.randomValue(),
	val userId: UserId,
	val tags: Set<String>,
	val numberOfRepetitions: Int,
	val period: Period,
	val message: String?,
) : Command()