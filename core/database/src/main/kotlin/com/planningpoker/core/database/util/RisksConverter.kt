package com.planningpoker.core.database.util

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Risks are always read and replaced together and never queried, so one JSON column is enough. */
internal class RisksConverter {

    private val serializer = ListSerializer(String.serializer())

    @TypeConverter
    fun fromRisks(risks: List<String>): String = Json.encodeToString(serializer, risks)

    @TypeConverter
    fun toRisks(json: String): List<String> = Json.decodeFromString(serializer, json)
}
