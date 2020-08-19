package mbtinder.android.component

import mbtinder.lib.component.IDContent
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class CommandResult : JSONParsable, IDContent {
    lateinit var uuid: UUID
    lateinit var arguments: JSONObject

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(uuid: UUID, arguments: JSONObject) {
        this.uuid = uuid
        this.arguments = arguments

        updateJSONObject()
    }

    override fun getUUID() = uuid
}