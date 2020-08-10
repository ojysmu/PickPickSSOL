package mbtinder.lib.util

import java.util.*

object RandomUtil {
    private val random = Random(System.currentTimeMillis())

    fun getRandomInt(max: Int): Int {
        return random.nextInt(max)
    }

    fun getRandomString(length: Int): String {
        val stringBuilder = StringBuilder()
        repeat(length) {
            stringBuilder.append(random.nextInt(Char.MAX_VALUE.toInt()).toChar())
        }

        return stringBuilder.toString()
    }
}