package mbtinder.lib.util

import mbtinder.lib.component.json.JSONContent
import org.json.JSONArray
import org.json.JSONObject

class JSONList<T: JSONContent>() : ArrayList<T>() {
    constructor(list: List<T>): this() {
        addAll(list)
    }

    constructor(mutableList: AbstractMutableList<T>): this() {
        addAll(mutableList)
    }

    constructor(collection: Collection<T>): this() {
        addAll(collection)
    }

    constructor(jsonArray: JSONArray, clazz: Class<T>): this() {
        addAll((0 until jsonArray.length()).mapTo(JSONList<T>()) {
            clazz.getDeclaredConstructor(JSONObject::class.java).newInstance(jsonArray.getJSONObject(it))
        })
    }

    fun toJSONArray(): JSONArray {
        val jsonArray = JSONArray()
        forEach {
            it.updateJSONObject()
            jsonArray.put(it.toJSONObject())
        }

        return jsonArray
    }

    fun updateJSONObject() = forEach { it.updateJSONObject() }
}

fun <T: JSONContent> jsonListOf(vararg elements: T): JSONList<T> {
    return if (elements.isEmpty()) {
        JSONList()
    } else {
        JSONList<T>().apply { addAll(elements) }
    }
}

fun <T: JSONContent> Collection<T>.toJSONList(): JSONList<T> {
    return JSONList(this)
}

inline fun <reified T: JSONContent> JSONArray.toJSONList(): JSONList<T> {
    return JSONList(this, T::class.java)
}

fun toJSONArray() = JSONArray().apply { forEach { put(it) } }