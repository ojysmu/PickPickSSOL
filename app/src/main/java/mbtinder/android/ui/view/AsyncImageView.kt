package mbtinder.android.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import mbtinder.android.io.http.ImageDownloader
import mbtinder.android.util.ImageUtil
import mbtinder.android.util.Log
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.ImageComponent

class AsyncImageView: AppCompatImageView {
    constructor(context: Context): super(context, null, 0)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    private fun setImageFromServer(imageComponent: ImageComponent) {
        ImageDownloader(imageComponent, this).execute()
//        ImageDownloader(imageComponent, this).start()
    }

    fun setImage(imageComponent: ImageComponent) {
        if (!imageComponent.hasImage()) {
            setImageFromServer(imageComponent)
        } else {
            setImageBitmap(ImageUtil.byteArrayToBitmap(imageComponent.getImage()))
        }
    }
}