package mbtinder.server

import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.socket.SocketServer
import mbtinder.server.util.UserUtil

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SocketServer.createInstance(8080).start()
            MySQLServer.createInstance("localhost", "mysql", "root", "kimju888").start()


        }
    }
}