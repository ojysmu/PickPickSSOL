package mbtinder.android.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.get
import mbtinder.lib.util.sum
import java.io.ByteArrayOutputStream

private typealias RGBColor = Triple<Int, Int, Int>

object ImageUtil {
    private const val RESIZE_DEFAULT = 640

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

    fun resizeByteArray(byteArray: ByteArray, max: Int = RESIZE_DEFAULT): ByteArray {
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

    fun getColorAverage(bitmap: Bitmap): RGBColor {
        var rBucket: Long = 0
        var gBucket: Long = 0
        var bBucket: Long = 0
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap[x, y]
                rBucket += Color.red(pixel)
                gBucket += Color.green(pixel)
                bBucket += Color.blue(pixel)
            }
        }

        return Triple((rBucket / size).toInt(), (gBucket / size).toInt(), (bBucket / size).toInt())
    }

    fun isLight(color: RGBColor): Boolean {
        return color.sum() / 3 >= 128
    }

    fun isDark(color: RGBColor): Boolean {
        return color.sum() / 3 < 128
    }
}