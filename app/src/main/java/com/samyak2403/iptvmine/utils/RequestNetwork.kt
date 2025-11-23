package com.samyak2403.iptvmine.utils

import android.os.Handler
import android.os.Looper
import okhttp3.*
import java.io.IOException

class RequestNetwork {

    interface RequestListener {
        fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>)
        fun onErrorResponse(tag: String, message: String)
    }

    fun startRequestNetwork(
        method: String,
        url: String,
        tag: String,
        listener: RequestListener
    ) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    listener.onErrorResponse(tag, e.message ?: "Unknown error")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Handler(Looper.getMainLooper()).post {
                    listener.onResponse(tag, responseBody, HashMap())
                }
            }
        })
    }

    companion object {
        const val GET = "GET"
        const val POST = "POST"
    }
}
