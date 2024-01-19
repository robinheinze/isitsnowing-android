package com.example.isitsnowing

import okhttp3.Request
import okhttp3.Response
import okhttp3.OkHttpClient

class SendRequest {
    fun makeRequest(url: String): String{
        val okHttpClient = OkHttpClient()
        return parseResponse(okHttpClient.newCall(createRequest(url)).execute())

    }

    private fun createRequest(url: String): Request {
        return Request.Builder().url(url).build()
    }

    private fun parseResponse(response: Response): String {
        return response.body?.string() ?:""
    }
}