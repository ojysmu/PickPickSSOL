package mbtinder.android.io.http

import android.os.AsyncTask
import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.util.Log
import mbtinder.lib.constant.ServerPath
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.util.*

class SQLiteDownloader(private val userId: UUID, private val databaseDir: File) : AsyncTask<Void, Void, Unit>() {
    override fun doInBackground(vararg params: Void?) {
        val url = URL(ServerPath.getUserDatabaseUrl(userId))
        val connection = url.openConnection()
        connection.connect()

        try {
            val inputStream = connection.getInputStream()
            val bytes = inputStream.readBytes()
            if (databaseDir.exists()) {
                databaseDir.delete()
            }
            databaseDir.createNewFile()
            databaseDir.outputStream().write(bytes)
            inputStream.close()

            SQLiteConnection.createInstance(databaseDir)
        } catch (e: FileNotFoundException) {
            Log.e("Failed to download database", e)
        }
    }
}