package mbtinder.android.ui.view

import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import mbtinder.android.util.ImageDownloader
import mbtinder.android.util.ImageUtil
import mbtinder.android.util.Log
import mbtinder.lib.component.ImageComponent
import java.io.FileNotFoundException
import java.net.URL

class AsyncImageView: AppCompatImageView {
    constructor(context: Context): super(context, null, 0)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    fun setImageFromServer(imageComponent: ImageComponent) {
        val result = ImageDownloader(imageComponent).execute().get()
        result?.let {
            setImageBitmap(ImageUtil.byteArrayToBitmap(result))
            imageComponent.setImage(result)
        }
    }

    fun setImage(imageComponent: ImageComponent) {
        if (!imageComponent.hasImage()) {
            setImageFromServer(imageComponent)
        } else {
            setImageBitmap(ImageUtil.byteArrayToBitmap(imageComponent.getImage()))
        }
    }
}