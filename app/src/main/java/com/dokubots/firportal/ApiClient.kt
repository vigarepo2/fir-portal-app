package com.dokubots.firportal
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
object ApiClient {
    const val BASE_URL = "https://fir.vs8.in"
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
}
