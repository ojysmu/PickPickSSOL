package mbtinder.android.io.http

import mbtinder.android.util.Log
import mbtinder.lib.constant.ServerPath
import mbtinder.lib.util.BlockWrapper
import mbtinder.lib.util.blockNull
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class ImageUploader(private val userId: UUID, private val rawImage: ByteArray, private val needResult: Boolean = false): Thread() {
    private var resultCode = -1
    private var result: JSONObject? = null

    override fun run() {
        val lineEnd = "\r\n"
        val twoHyphens = "--"
        val boundary = "*****"
        val url = URL("${ServerPath.WEB_PROTOCOL}://${ServerPath.ADDRESS}:${ServerPath.PORT_WEB}/upload_user_profile.php" +
                "?user_id=${URLEncoder.encode(userId.toString(), "UTF-8")}")

        val connection = (url.openConnection() as HttpURLConnection).apply {
            doInput = true
            doOutput = true
            useCaches = false
            requestMethod = "POST"
            addRequestProperty("Connection", "Keep-Alive")
            addRequestProperty("ENCTYPE", "multipart/form-data")
            addRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        DataOutputStream(connection.outputStream).apply {
            writeBytes(twoHyphens + boundary + lineEnd)
            writeBytes("""Content-Disposition: form-data; name="uploaded_file"; filename="$userId.png"$lineEnd""")
            writeBytes(lineEnd)
            write(rawImage)
            writeBytes(lineEnd)
            writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
        }

        if (needResult) {
            DataInputStream(connection.inputStream).apply {
                result = JSONObject(String(readBytes()))
                Log.v("ImageUploader.run(): result=$result")
            }
        }
    }

    fun getResultCode() = blockNull(BlockWrapper(resultCode)) { it == -1 }!!

    fun getResult(): JSONObject {
        while (result == null) {
            sleep(100)
        }

        return result!!
    }
}