package mbtinder.android.io.http

import android.os.AsyncTask
import mbtinder.android.util.Log
import mbtinder.lib.constant.ServerPath
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.util.*

class SQLiteDownloader(private val userId: UUID, private val filesDir: String) : AsyncTask<Void, Void, Unit>() {
    override fun doInBackground(vararg params: Void?) {
        val url = URL(ServerPath.getUserDatabaseUrl(userId))
        val connection = url.openConnection()
        connection.connect()

        try {
            val inputStream = connection.getInputStream()
            val bytes = inputStream.readBytes()
            val destFile = File("$filesDir/table.db")
            if (destFile.exists()) {
                destFile.delete()
            }
            destFile.createNewFile()
            destFile.outputStream().write(bytes)
            inputStream.close()
        } catch (e: FileNotFoundException) {
            Log.e("Failed to download database", e)
        }
    }
}