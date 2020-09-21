package com.j0rsa.cracker.tracker

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.lang.reflect.Type

object Serializer {
	val gson: Gson = GsonBuilder()
		.registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter().nullSafe())
		.registerTypeAdapter(Event::class.java, EventDeserializer())
		.create()

	inline fun <reified T> toJson(o: T): String = gson.toJson(o, T::class.java)
	inline fun <reified T> fromJson(s: String): T = gson.fromJson(s, T::class.java)
}

class DateTimeTypeAdapter : TypeAdapter<DateTime>() {

	override fun write(out: JsonWriter, value: DateTime) {
		out.value(ISODateTimeFormat.dateTime().print(value))
	}

	override fun read(input: JsonReader): DateTime =
		ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(input.nextString())
}

class EventDeserializer : JsonDeserializer<Event?> {

	@Throws(JsonParseException::class)
	override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Event? = run {
		val jsonObject = json.asJsonObject
		val typeElement = jsonObject.get(Event::type.name)
		EventTypes.findValue(typeElement.asString)
			?.let { Serializer.gson.fromJson(jsonObject, it.kClass.java) }
	}
}