package com.j0rsa.cracker.tracker.service

import com.j0rsa.cracker.tracker.handler.ValueTemplateRow
import com.j0rsa.cracker.tracker.model.Habit
import com.j0rsa.cracker.tracker.model.ValueTemplate
import com.j0rsa.cracker.tracker.model.ValueTemplates
import org.jetbrains.exposed.sql.deleteWhere

object ValueTemplateService {
	private fun create(row: ValueTemplateRow, habit: Habit) = ValueTemplate.new {
		this.habit = habit
		type = row.type
		this.values = row.values
		this.name = row.name
	}

	fun create(rows: List<ValueTemplateRow>, habit: Habit) = rows.map {
        create(
            it,
            habit
        )
    }

	fun reCreate(rows: List<ValueTemplateRow>, habit: Habit) {
		ValueTemplates.deleteWhere { ValueTemplates.habitId eq habit.id }
		rows.map { create(it, habit) }
	}

	fun findAll() = ValueTemplate.all().toList()
}