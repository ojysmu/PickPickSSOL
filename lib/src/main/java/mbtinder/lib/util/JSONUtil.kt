package mbtinder.lib.util

import mbtinder.lib.component.json.JSONContent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun JSONArray.saveJSONArray(path: String) = saveJSONString(toString(), path)

fun loadJSONArray(path: String): JSONArray {
    val file = File(path)

    FileInputStream(file).apply {
        val rawArray = String(readBytes())
        val result = JSONArray(rawArray)
        close()

        return result
    }
}

inline fun <reified T: JSONContent> loadJSONList(path: String) = loadJSONArray(path).toJSONList<T>()

fun JSONObject.saveJSONObject(path: String) = saveJSONString(toString(), path)

fun JSONObject.loadJSONObject(path: String): JSONObject {
    val file = File(path)

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