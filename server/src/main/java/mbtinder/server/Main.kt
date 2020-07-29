package mbtinder.server

import mbtinder.lib.io.component.CommandContent
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.socket.SocketServer
import org.json.JSONObject

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val c = CommandContent(JSONObject("{\"arguments\":{\"email\":\"kjiwhan0120@gmail.com\"},\"name\":\"CHECK_EMAIL_DUPLICATED\",\"uuid\":\"b3aee25c-6f09-4562-8207-e14e5654cf66\"}"))
            println(c.name)

            SocketServer.createInstance(8080).start()
            MySQLServer.createInstance("localhost", "mysql", "root", "kimju888").start()
        }
    }
}