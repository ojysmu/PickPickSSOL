package mbtinder.android.util.io

import mbtinder.android.io.SocketClient
import mbtinder.android.util.io.component.ServerResult
import mbtinder.lib.component.json.JSONParsable
import mbtinder.lib.io.component.CommandContent
import mbtinder.lib.io.constant.Command
import mbtinder.lib.util.JSONList
import mbtinder.lib.util.toJSONList
import org.json.JSONObject
import java.util.*

object SocketUtil {
    fun getServerResult(command: Command, arguments: JSONObject): JSONObject {
        val commandId = UUID.randomUUID()
        val client = SocketClient.getInstance()
        client.addCommand(CommandContent(commandId, command.name, arguments))
        return client.getResult(commandId)
    }

    fun getVoidResult(result: JSONObject): ServerResult<Void> {
        return if (result.getBoolean("result")) {
            ServerResult(true)
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }

    inline fun <reified T: JSONParsable> getSingleResult(result: JSONObject, fieldName: String): ServerResult<T> {
        return if (result.getBoolean("result")) {
            ServerResult(true, 0,
                T::class.java
                    .getDeclaredConstructor(JSONObject::class.java)
                    .newInstance(result.getJSONObject(fieldName)))
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }

    inline fun <reified T: JSONParsable> getJSONListResult(result: JSONObject, fieldName: String): ServerResult<JSONList<T>> {
        return if (result.getBoolean("result")) {
            ServerResult(true, 0, result.getJSONArray(fieldName).toJSONList())
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }

    fun checkEmailDuplicated(email: String): ServerResult<Void> {
        val arguments = JSONObject().apply { put("email", email) }
        return getVoidResult(getServerResult(Command.CHECK_EMAIL_DUPLICATED, arguments))
    }

    fun signUp(email: String, password: String, name: String, age: Int, gender: Int): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("email", email)
        arguments.put("password", password)
        arguments.put("name", name)
        arguments.put("age", age)
        arguments.put("gender", gender)

        return getVoidResult(getServerResult(Command.ADD_USER, arguments))
    }
}