package mbtinder.android.util

import android.os.Bundle
import org.json.JSONObject
import java.util.*

fun Bundle.putUUID(name: String, uuid: UUID) = putString(name, uuid.toString())

fun Bundle.getUUID(name: String) = getString(name)?.let { UUID.fromString(it) }

fun Bundle.putJSONObject(name: String, jsonObject: JSONObject) = putString(name, jsonObject.toString())

fun Bundle.getJSONObject(name: String) = getString(name)?.let { JSONObject(it) }