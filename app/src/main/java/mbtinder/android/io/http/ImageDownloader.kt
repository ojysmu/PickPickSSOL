package mbtinder.android.io.http

import android.os.AsyncTask
import mbtinder.android.util.Log
import mbtinder.lib.component.ImageComponent
import java.io.FileNotFoundException
import java.net.URL

class ImageDownloader(private val imageComponent: ImageComponent): AsyncTask<Void, Void, ByteArray?>() {
    override fun doInBackground(vararg params: Void?): ByteArray? {
        val url = URL(imageComponent.getImageUrl())
        val connection = url.openConnection()
        connection.connect()
        return try {
            val inputStream = connection.getInputStream()
            val bytes = inputStream.readBytes()
            inputStream.close()
            bytes
        } catch (e: FileNotFoundException) {
            Log.e("Failed to download image", e)
            null
        }
    }
}