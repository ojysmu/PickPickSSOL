package mbtinder.lib.util

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.sql.Date
import java.util.*

fun JSONObject.clone(): JSONObject {
    val cloned = JSONObject()
    keySet().forEach {
        cloned.put(it, get(it))
    }

    return cloned
}

fun JSONObject.getUUID(key: String): UUID = UUID.fromString(getString(key))

fun JSONObject.putUUID(key: String, uuid: UUID): JSONObject = put(key, uuid.toString())

fun JSONObject.getDate(key: String): Date = Date.valueOf(getString(key))

fun JSONObject.putDate(key: String, date: Date): JSONObject = put(key, date.toString())

fun JSONArray.getUUID(index: Int): UUID = UUID.fromString(getString(index))

fun JSONArray.putUUID(uuid: UUID): JSONArray = put(uuid.toString())

fun List<UUID>.toJSONArray() = JSONArray().also {
    forEach { element -> it.putUUID(element) }
}

fun JSONArray.toUUIDList() = (0 until length()).map { getUUID(it) }

fun JSONArray.saveJSONArray(path: String) = saveJSONString(toString(), path)

fun loadJSONArray(path: String): JSONArray {
    val file = File(path)
    if (!file.exists()) {
        return JSONArray()
    }

    FileInputStream(file).apply {
        val rawArray = String(readBytes())
        val result = JSONArray(rawArray)
        close()

        return result
    }
}

fun JSONObject.saveJSONObject(path: String) = saveJSONString(toString(), path)

fun loadJSONObject(path: String): JSONObject {
    val file = File(path)
    if (!file.exists()) {
        return JSONObject()
    }

    FileInputStream(file).apply {
        val rawArray = String(readBytes())
        val result = JSONObject(rawArray)
        close()

        return result
    }
}

private fun saveJSONString(jsonString: String, path: String) {
    val file = File(path)
    if (file.exists()) {
        file.delete()
    }

    file.createNewFile()

    val fileOutputStream = FileOutputStream(file)
    fileOutputStream.write(jsonString.toByteArray())
    fileOutputStream.flush()
    fileOutputStream.close()
}