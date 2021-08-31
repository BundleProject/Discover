package dev.isxander.xanderlib.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object HttpsUtils {

    private fun getResponse(url: String): Response? {
        try {
            val client = OkHttpClient()
            val request: Request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Discover/1.0")
                .build()
            return client.newCall(request).execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getBytes(url: String): ByteArray? {
        try {
            return getResponse(url)!!.body!!.bytes()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getString(url: String): String? {
        try {
            return getResponse(url)!!.body!!.string()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    fun downloadFile(url: String, file: File) {
        val bytes = getBytes(url)
        if (bytes != null) {
            val fos = FileOutputStream(file)
            fos.write(bytes)
            fos.close()
        } else {
            throw IOException("Bytes from URL was null!")
        }
    }

}