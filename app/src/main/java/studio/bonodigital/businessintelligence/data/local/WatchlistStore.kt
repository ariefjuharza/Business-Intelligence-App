package studio.bonodigital.businessintelligence.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "bi_settings")

/**
 * WatchlistStore bertanggung jawab untuk menyimpan dan mengambil data persisten aplikasi
 * seperti daftar pantauan (watchlist) saham dan URL basis API menggunakan Jetpack DataStore.
 */
class WatchlistStore(private val context: Context) {
    companion object {
        private val WATCHLIST_KEY = stringPreferencesKey("watchlist_tickers")
        private val API_URL_KEY = stringPreferencesKey("api_base_url")
    }

    val watchlistFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val csv = preferences[WATCHLIST_KEY] ?: "BBCA,BBRI,BMRI,TLKM,ASII,GOTO,BREN,UNVR"
        if (csv.isEmpty()) emptyList() else csv.split(",")
    }

    val apiUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_URL_KEY] ?: "https://bi-api.bonodigital.biz.id"
    }

    suspend fun saveWatchlist(tickers: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[WATCHLIST_KEY] = tickers.joinToString(",")
        }
    }

    suspend fun addToWatchlist(ticker: String) {
        context.dataStore.edit { preferences ->
            val currentCsv = preferences[WATCHLIST_KEY] ?: "BBCA,BBRI,BMRI,TLKM,ASII,GOTO,BREN,UNVR"
            val currentList = if (currentCsv.isEmpty()) emptyList() else currentCsv.split(",")
            val cleanTicker = ticker.trim().uppercase()
            if (!currentList.contains(cleanTicker)) {
                val newList = currentList + cleanTicker
                preferences[WATCHLIST_KEY] = newList.joinToString(",")
            }
        }
    }

    suspend fun removeFromWatchlist(ticker: String) {
        context.dataStore.edit { preferences ->
            val currentCsv = preferences[WATCHLIST_KEY] ?: "BBCA,BBRI,BMRI,TLKM,ASII,GOTO,BREN,UNVR"
            val currentList = if (currentCsv.isEmpty()) emptyList() else currentCsv.split(",")
            val cleanTicker = ticker.trim().uppercase()
            if (currentList.contains(cleanTicker)) {
                val newList = currentList - cleanTicker
                preferences[WATCHLIST_KEY] = newList.joinToString(",")
            }
        }
    }

    suspend fun saveApiUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[API_URL_KEY] = url
        }
    }
}
