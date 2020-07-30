package mbtinder.server

import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.socket.SocketServer

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SocketServer.createInstance(8080).start()
            MySQLServer.createInstance("localhost", "mbtinder", "root", "kimju888").start()
        }
    }
}