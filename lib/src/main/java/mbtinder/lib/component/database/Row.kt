package mbtinder.lib.component.database

import org.json.JSONArray
import org.json.JSONObject
import java.sql.Date
import java.util.*
import kotlin.collections.HashMap

class Row : HashMap<String, Any>() {
    fun getString(column: String) = get(column) as String

    fun getInt(column: String) = get(column) as Int

    fun getLong(column: String) = get(column) as Long

    fun getDouble(column: String) = get(column) as Double

    fun getBoolean(column: String) = get(column) as Boolean

    fun getUUID(column: String) = UUID.fromString(getString(column))

    fun getJSONObject(column: String) = JSONObject(getString(column))

    fun getJSONArray(column: String) = JSONArray(getString(column))

    fun getDate(column: String) = get(column) as Date
}