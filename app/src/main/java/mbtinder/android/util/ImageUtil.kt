package mbtinder.android.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.io.ByteArrayOutputStream

object ImageUtil {
    fun drawableToByteArray(drawable: Drawable): ByteArray {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun byteArrayToDrawable(context: Context, bytes: ByteArray): Drawable {
        return BitmapDrawable(context.resources, BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
    }

    fun byteArrayToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        return stream.toByteArray()
    }

    fun resizeBitmap(bitmap: Bitmap, ratio: Int): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, ratio, stream)

        return byteArrayToBitmap(stream.toByteArray())
    }

    fun resizeByteArray(byteArray: ByteArray, max: Int): ByteArray {
        val options = BitmapFactory.Options()
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

        var width = options.outWidth
        var height = options.outHeight
        var sampleSize = 1

        while (width > max || height > max) {
            width /= 2
            height /= 2
            sampleSize *= 2
        }
        options.inSampleSize = sampleSize

        val resizedBitmap =  BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

        return bitmapToByteArray(resizedBitmap)
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0 || degrees == 360) {
            return bitmap
        }

        val matrix = Matrix().apply { setRotate(degrees.toFloat(), bitmap.width.toFloat(), bitmap.height.toFloat()) }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}