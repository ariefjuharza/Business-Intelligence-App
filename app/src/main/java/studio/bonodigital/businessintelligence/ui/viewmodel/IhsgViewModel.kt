package studio.bonodigital.businessintelligence.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.bonodigital.businessintelligence.data.repository.BiRepository
import java.util.Date

/**
 * Representasi data saham tunggal untuk tampilan Dashboard IHSG.
 */
data class IhsgStockItem(
    val ticker: String,
    val name: String,
    val sector: String,
    val price: Double,
    val change: Double,
    val pct: Double,
    val score: Double?,
    val label: String?,
    val ok: Boolean
)

/**
 * State UI untuk mengelola status pemuatan dan data saham IHSG.
 */
sealed interface IhsgUiState {
    object Loading : IhsgUiState
    data class Success(val stocks: List<IhsgStockItem>) : IhsgUiState
    data class Error(val message: String) : IhsgUiState
}

/**
 * ViewModel untuk mengelola logika data Dashboard IHSG (Indeks Harga Saham Gabungan).
 */
class IhsgViewModel(private val repository: BiRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<IhsgUiState>(IhsgUiState.Loading)
    val uiState: StateFlow<IhsgUiState> = _uiState.asStateFlow()

    private val _lastUpdate = MutableStateFlow<Date?>(null)
    val lastUpdate: StateFlow<Date?> = _lastUpdate.asStateFlow()

    private val idxStocks = listOf(
        Pair("BBCA", Pair("Bank Central Asia", "Perbankan")),
        Pair("BBRI", Pair("Bank Rakyat Indonesia", "Perbankan")),
        Pair("BMRI", Pair("Bank Mandiri", "Perbankan")),
        Pair("TLKM", Pair("Telkom Indonesia", "Telecom")),
        Pair("ASII", Pair("Astra International", "Otomotif")),
        Pair("GOTO", Pair("GoTo Gojek Tokopedia", "Teknologi")),
        Pair("BREN", Pair("Barito Renewables", "Energi")),
        Pair("UNVR", Pair("Unilever Indonesia", "FMCG"))
    )

    init {
        startAutoRefresh()
    }

    /**
     * Mengambil data saham dari API untuk daftar saham IDX pilihan.
     */
    fun fetchIhsgData() {
        viewModelScope.launch {
            _uiState.value = IhsgUiState.Loading
            try {
                // Inisialisasi URL Dasar terlebih dahulu
                repository.initApiUrl()
                
                val jobs = idxStocks.map { (ticker, details) ->
                    val (name, sector) = details
                    async {
                        try {
                            val res = repository.getBiAnalysis(ticker)
                            val stock = res.data?.stockData
                            val sent = res.data?.sentiment
                            IhsgStockItem(
                                ticker = ticker,
                                name = name,
                                sector = sector,
                                price = stock?.currentPrice ?: 0.0,
                                change = stock?.priceChange ?: 0.0,
                                pct = stock?.priceChangePercent ?: 0.0,
                                score = sent?.score,
                                label = sent?.label,
                                ok = true
                            )
                        } catch (_: Exception) {
                            IhsgStockItem(
                                ticker = ticker,
                                name = name,
                                sector = sector,
                                price = 0.0,
                                change = 0.0,
                                pct = 0.0,
                                score = null,
                                label = null,
                                ok = false
                            )
                        }
                    }
                }
                val results = jobs.awaitAll()
                _uiState.value = IhsgUiState.Success(results)
                _lastUpdate.value = Date()
            } catch (e: Exception) {
                _uiState.value = IhsgUiState.Error(e.message ?: "Gagal memuat daftar saham IDX")
            }
        }
    }

    /**
     * Memulai proses penyegaran data otomatis setiap 60 detik.
     */
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                fetchIhsgData()
                delay(60000) // 60 detik
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return IhsgViewModel(BiRepository(context.applicationContext)) as T
            }
        }
    }
}
