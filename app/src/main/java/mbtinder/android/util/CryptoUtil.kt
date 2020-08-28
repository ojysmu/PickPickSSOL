package mbtinder.android.util

import org.bouncycastle.jcajce.provider.digest.Keccak

object CryptoUtil {
    private fun getHeader(length: Int) = String(CharArray(40 - length) { 'P' })

    fun encrypt(plainText: String): String {
        val digester = Keccak.DigestKeccak(512)
        digester.update((getHeader(plainText.length) + plainText).toByteArray(charset("UTF-8")))
        return org.bouncycastle.util.encoders.Hex.toHexString(digester.digest())
    }
}