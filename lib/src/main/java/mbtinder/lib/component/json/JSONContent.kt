package mbtinder.lib.component.json

import org.json.JSONObject

interface JSONContent {
    fun toJSONObject(): JSONObject

    fun updateJSONObject() {}
}