package mbtinder.lib.io.component

import mbtinder.lib.component.IDContent
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class CommandContent: JSONParsable, IDContent {
    lateinit var uuid: UUID
    lateinit var name: String
    lateinit var arguments: JSONObject

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(uuid: UUID, name: String, arguments: JSONObject) {
        this.uuid = uuid
        this.name = name
        this.arguments = arguments

        updateJSONObject()
    }

    override fun getUUID() = uuid
}