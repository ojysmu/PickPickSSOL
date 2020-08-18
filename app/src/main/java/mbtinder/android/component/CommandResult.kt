package mbtinder.android.component

import com.google.gson.Gson
import mbtinder.lib.component.IDContent
import mbtinder.lib.component.json.GSONParsable
import org.json.JSONObject
import java.util.*

class CommandResult : GSONParsable<CommandResult>, IDContent {
    var uuid: UUID
    var arguments: JSONObject

    constructor(uuid: UUID, arguments: JSONObject): super(CommandResult::class.java) {
        this.uuid = uuid
        this.arguments = arguments
    }

    constructor(json: String): super(json, CommandResult::class.java) {
        this.uuid = parsed.uuid
        this.arguments = parsed.arguments
    }

    override fun getUUID() = uuid

    override fun serialize(): String = Gson().toJson(this)

    override fun deserialize(json: String) {
        Gson().fromJson<CommandResult>(json, this::class.java).also {
            this.uuid = it.uuid
            this.arguments = it.arguments
        }
    }
}