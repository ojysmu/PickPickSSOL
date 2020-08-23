package mbtinder.android.io.socket

import mbtinder.android.io.component.ServerResult
import mbtinder.android.util.Log
import mbtinder.android.util.runOnBackground
import mbtinder.lib.component.json.JSONParsable
import mbtinder.lib.io.component.CommandContent
import mbtinder.lib.io.constant.Command
import mbtinder.lib.util.JSONList
import mbtinder.lib.util.block
import mbtinder.lib.util.toJSONList
import org.json.JSONObject
import java.util.*

object SocketUtil {
    /**
     * 서버로 요청을 보낸 다음 응답을 기다림
     *
     * @param command: 보낼 명령
     * @param arguments: 명령의 인수
     * @return 명령 처리 결과
     */
    fun getServerResult(command: Command, arguments: JSONObject): JSONObject {
        val commandId = UUID.randomUUID()
        val client = SocketClient.getInstance()
        client.addCommand(CommandContent(commandId, command.name, arguments))
        return client.getResult(commandId)
    }

    fun getVoidResult(result: JSONObject): ServerResult<Void> {
        Log.v("getVoidResult: result=$result")

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
}