package studio.bonodigital.businessintelligence.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import studio.bonodigital.businessintelligence.data.local.WatchlistStore
import studio.bonodigital.businessintelligence.data.model.BiResponse
import studio.bonodigital.businessintelligence.data.model.IhsgDashboardResponse
import studio.bonodigital.businessintelligence.data.network.NetworkModule
import okhttp3.WebSocket

/**
 * BiRepository bertindak sebagai mediator antara sumber data (DataStore dan API)
 * serta menyediakan metode untuk mengakses analisis bisnis dan data IHSG.
 */
class BiRepository(context: Context) {
    private val watchlistStore = WatchlistStore(context)

    val watchlistFlow: Flow<List<String>> = watchlistStore.watchlistFlow
    val apiUrlFlow: Flow<String> = watchlistStore.apiUrlFlow

    suspend fun initApiUrl() {
        val url = watchlistStore.apiUrlFlow.first()
        NetworkModule.updateBaseUrl(url)
    }

    suspend fun saveApiUrl(url: String) {
        watchlistStore.saveApiUrl(url)
        NetworkModule.updateBaseUrl(url)
    }

    suspend fun getBiAnalysis(ticker: String): BiResponse {
        initApiUrl()
        return NetworkModule.getApiService().getBiAnalysis(ticker = ticker)
    }

    suspend fun getIhsgDashboard(): IhsgDashboardResponse {
        initApiUrl()
        return NetworkModule.getApiService().getIhsgDashboard()
    }

    fun startWebSocket(
        ticker: String,
        onMessage: (String) -> Unit,
        onStatus: (String) -> Unit,
        onError: (Throwable) -> Unit
    ): WebSocket {
        return NetworkModule.createWebSocket(ticker, onMessage, onStatus, onError)
    }

    suspend fun addToWatchlist(ticker: String) {
        watchlistStore.addToWatchlist(ticker)
    }

    suspend fun removeFromWatchlist(ticker: String) {
        watchlistStore.removeFromWatchlist(ticker)
    }
}
