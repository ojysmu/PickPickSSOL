package mbtinder.android.ui.view

import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import mbtinder.android.util.ImageUtil
import mbtinder.android.util.Log
import mbtinder.lib.component.ImageComponent
import java.io.FileNotFoundException
import java.net.URL

class AsyncImageView: AppCompatImageView {
    constructor(context: Context): super(context, null, 0)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    /**
     * 이미지 다운로드 AsyncTask
     * @param url: 대상 URL
     */
    private class ImageDownloader(private val imageView: AppCompatImageView,
                                  private val imageComponent: ImageComponent): AsyncTask<Void, Void, ByteArray?>() {
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
                imageView.setImageBitmap(ImageUtil.byteArrayToBitmap(result))
                imageComponent.setImage(result)
            }
        }
    }

    fun setImageFromServer(imageComponent: ImageComponent) {
        ImageDownloader(this, imageComponent).execute()
    }

    fun setImage(imageComponent: ImageComponent) {
        if (!imageComponent.hasImage()) {
            setImageFromServer(imageComponent)
        } else {
            setImageBitmap(ImageUtil.byteArrayToBitmap(imageComponent.getImage()))
        }
    }
}