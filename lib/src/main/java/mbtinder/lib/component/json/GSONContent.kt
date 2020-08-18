package mbtinder.lib.component.json

import com.google.gson.Gson

interface GSONContent {
    fun serialize(): String = Gson().toJson(this)

    fun deserialize(json: String)
}