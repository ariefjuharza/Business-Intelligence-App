package studio.bonodigital.businessintelligence.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import studio.bonodigital.businessintelligence.data.model.BiResponse
import studio.bonodigital.businessintelligence.data.repository.BiRepository

sealed interface BiUiState {
    object Idle : BiUiState
    object Loading : BiUiState
    data class Success(val response: BiResponse) : BiUiState
    data class Error(val message: String) : BiUiState
}

class BiViewModel(private val repository: BiRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BiUiState>(BiUiState.Idle)
    val uiState: StateFlow<BiUiState> = _uiState.asStateFlow()

    private val _wsStatus = MutableStateFlow("idle")
    val wsStatus: StateFlow<String> = _wsStatus.asStateFlow()

    private val _countdown = MutableStateFlow(30)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private val _isFavorited = MutableStateFlow(false)
    val isFavorited: StateFlow<Boolean> = _isFavorited.asStateFlow()

    val watchlist: StateFlow<List<String>> = repository.watchlistFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activeWebSocket: WebSocket? = null
    private var countdownJob: Job? = null
    private var autoTickJob: Job? = null
    private var currentTicker: String = ""

    fun searchTicker(ticker: String, forceHttp: Boolean = false) {
        val cleanTicker = ticker.trim().uppercase()
        if (cleanTicker.isEmpty()) return

        currentTicker = cleanTicker
        _uiState.value = BiUiState.Loading
        checkIfFavorited()

        if (forceHttp) {
            fetchAnalysisHttp(cleanTicker)
        } else {
            connectWebSocket(cleanTicker)
        }
    }

    private fun connectWebSocket(ticker: String) {
        cleanupConnection()

        activeWebSocket = repository.startWebSocket(
            ticker = ticker,
            onMessage = { text ->
                viewModelScope.launch {
                    try {
                        val response = Gson().fromJson(text, BiResponse::class.java)
                        _uiState.value = BiUiState.Success(response)
                        startCountdown()
                    } catch (_: Exception) {
                        // In case of any parsing error, lets fallback to HTTP
                        fetchAnalysisHttp(ticker)
                    }
                }
            },
            onStatus = { status ->
                _wsStatus.value = status
            },
            onError = { _ ->
                // Fallback to Retrofit HTTP on connection issue
                fetchAnalysisHttp(ticker)
            }
        )
    }

    private fun fetchAnalysisHttp(ticker: String) {
        viewModelScope.launch {
            try {
                val response = repository.getBiAnalysis(ticker)
                _uiState.value = BiUiState.Success(response)
                startHttpAutoRefresh()
            } catch (e: Exception) {
                _uiState.value = BiUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        _countdown.value = 30
        countdownJob = viewModelScope.launch {
            while (_countdown.value > 0) {
                delay(1000)
                _countdown.value -= 1
            }
            // Trigger refresh tick when countdown hits 0
            triggerWebSocketTick()
        }
    }

    private fun triggerWebSocketTick() {
        val ws = activeWebSocket
        if (ws != null && _wsStatus.value == "connected") {
            val tickMessage = """{"ticker":"$currentTicker", "detail":"full"}"""
            ws.send(tickMessage)
            startCountdown()
        } else {
            // Reconnect or fallback
            searchTicker(currentTicker)
        }
    }

    private fun startHttpAutoRefresh() {
        autoTickJob?.cancel()
        countdownJob?.cancel()
        _countdown.value = 30
        
        countdownJob = viewModelScope.launch {
            while (_countdown.value > 0) {
                delay(1000)
                _countdown.value -= 1
            }
            // Trigger HTTP reload
            fetchAnalysisHttp(currentTicker)
        }
    }

    fun toggleFavorite(ticker: String) {
        viewModelScope.launch {
            val currentList = watchlist.value
            val cleanTicker = ticker.trim().uppercase()
            if (currentList.contains(cleanTicker)) {
                repository.removeFromWatchlist(cleanTicker)
                _isFavorited.value = false
            } else {
                repository.addToWatchlist(cleanTicker)
                _isFavorited.value = true
            }
        }
    }

    fun checkIfFavorited() {
        _isFavorited.value = watchlist.value.contains(currentTicker)
    }

    private fun cleanupConnection() {
        countdownJob?.cancel()
        autoTickJob?.cancel()
        activeWebSocket?.close(1000, "Clean termination")
        activeWebSocket = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanupConnection()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BiViewModel(BiRepository(context.applicationContext)) as T
            }
        }
    }
}
