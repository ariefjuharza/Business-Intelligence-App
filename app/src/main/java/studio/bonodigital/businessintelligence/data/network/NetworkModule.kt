package studio.bonodigital.businessintelligence.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private var currentBaseUrl: String = "https://bi-api.bonodigital.biz.id"
    private var apiServiceInstance: ApiService? = null
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getApiService(): ApiService {
        if (apiServiceInstance == null) {
            rebuildRetrofit(currentBaseUrl)
        }
        return apiServiceInstance!!
    }

    fun updateBaseUrl(newUrl: String) {
        val normalizedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        if (currentBaseUrl != normalizedUrl) {
            currentBaseUrl = normalizedUrl
            rebuildRetrofit(normalizedUrl)
        }
    }

    private fun rebuildRetrofit(baseUrl: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiServiceInstance = retrofit.create(ApiService::class.java)
    }

    // Convert HTTP URL to WebSocket URL
    fun getWebSocketUrl(): String {
        val base = currentBaseUrl.trim()
        return when {
            base.startsWith("https://") -> base.replace("https://", "wss://")
            base.startsWith("http://") -> base.replace("http://", "ws://")
            else -> "wss://bi-api.bonodigital.biz.id"
        }.replace(Regex("/$"), "") + "/ws"
    }

    fun createWebSocket(
        ticker: String,
        onMessage: (String) -> Unit,
        onStatus: (String) -> Unit,
        onError: (Throwable) -> Unit
    ): WebSocket {
        val wsUrl = getWebSocketUrl()
        val request = Request.Builder()
            .url(wsUrl)
            .build()

        onStatus("connecting")
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onStatus("connected")
                // Send subscription message
                val subscriptionMessage = """{"ticker":"$ticker", "detail":"full"}"""
                webSocket.send(subscriptionMessage)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onStatus("disconnected")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onStatus("error")
                onError(t)
            }
        }

        return okHttpClient.newWebSocket(request, listener)
    }
}
