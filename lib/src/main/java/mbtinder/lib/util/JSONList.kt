package mbtinder.lib.util

import mbtinder.lib.component.json.JSONContent
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONArray
import org.json.JSONObject

class JSONList<T: JSONContent> : ArrayList<T> {
    constructor()

    constructor(collection: Collection<T>) {
        addAll(collection)
    }

    constructor(jsonArray: JSONArray, clazz: Class<T>) {
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

    public override fun removeRange(start: Int, end: Int) {
        super.removeRange(start, end)
    }

    override fun toString() = toJSONArray().toString()
}

fun <T: JSONContent> Collection<T>.toJSONList() = JSONList(this)

fun toJSONArray() = JSONArray().apply { forEach { put(it) } }

inline fun <reified T: JSONContent> JSONArray.toJSONList() = JSONList(this, T::class.java)

inline fun <reified T: JSONParsable> JSONList<T>.add(element: JSONObject) =
    add(T::class.java.getDeclaredConstructor(JSONObject::class.java).newInstance(element))

fun <T: JSONContent> List<T>.toJSONArray() = JSONArray().also {
    forEach { element -> it.put(element.toJSONObject()) }
}