package mbtinder.android.io.http

import android.os.AsyncTask
import mbtinder.android.ui.view.AsyncImageView
import mbtinder.android.util.ImageUtil
import mbtinder.android.util.Log
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.ImageComponent
import mbtinder.lib.util.CloseableThread
import java.io.FileNotFoundException
import java.net.URL

class ImageDownloader(private val imageComponent: ImageComponent, private val asyncImageView: AsyncImageView? = null): AsyncTask<Void, Void, ByteArray?>() {
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

    override fun onPostExecute(result: ByteArray?) {
        result?.let {
            imageComponent.setImage(it)
            asyncImageView?.setImageBitmap(ImageUtil.byteArrayToBitmap(it))
        }
    }
}

//class ImageDownloader(private val imageComponent: ImageComponent, private val asyncImageView: AsyncImageView? = null): CloseableThread() {
//    init {
//        suffix = {
//            val url = URL(imageComponent.getImageUrl())
//            val connection = url.openConnection()
//            connection.connect()
//
//            val inputStream = connection.getInputStream()
//            val bytes = inputStream.readBytes()
//            inputStream.close()
//
//            imageComponent.setImage(bytes)
//            asyncImageView?.let {
//                runOnUiThread {
//                    it.setImageBitmap(ImageUtil.byteArrayToBitmap(bytes))
//                }
//            }
//        }
//    }
//}