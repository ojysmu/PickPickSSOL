package mbtinder.lib.component

import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject

class MBTIContent: JSONParsable {
    lateinit var title: String
    lateinit var content: String

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(title: String, content: String) {
        this.title = title
        this.content = content

        updateJSONObject()
    }
}