package studio.bonodigital.businessintelligence.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model data respons untuk endpoint analisis Business Intelligence (BI).
 * Digunakan untuk memetakan data dari GET /api/bi?ticker=...&detail=full.
 */
data class BiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("ticker") val ticker: String,
    @SerializedName("generated_at") val generatedAt: String,
    @SerializedName("data") val data: BiData?
)

/**
 * Kontainer utama untuk hasil analisis yang dikembalikan oleh API.
 */
data class BiData(
    @SerializedName("stock_data") val stockData: StockData?,
    @SerializedName("news_data") val newsData: NewsData?,
    @SerializedName("sentiment") val sentiment: SentimentData?,
    @SerializedName("recommendations") val recommendations: List<Recommendation>?,
    @SerializedName("analysis") val analysis: AnalysisData?,
    @SerializedName("top_positive_drivers") val topPositiveDrivers: List<String>?,
    @SerializedName("top_negative_drivers") val topNegativeDrivers: List<String>?
)

/**
 * Data historis dan statistik harga saham.
 */
data class StockData(
    @SerializedName("dates") val dates: List<String>?,
    @SerializedName("closing_prices") val closingPrices: List<Double>?,
    @SerializedName("current_price") val currentPrice: Double?,
    @SerializedName("price_change") val priceChange: Double?,
    @SerializedName("price_change_percent") val priceChangePercent: Double?,
    @SerializedName("average_price") val averagePrice: Double?,
    @SerializedName("highest_price") val highestPrice: Double?,
    @SerializedName("lowest_price") val lowestPrice: Double?,
    @SerializedName("volume") val volume: Long?
)

/**
 * Ringkasan data berita terkait emiten saham.
 */
data class NewsData(
    @SerializedName("source") val source: String?,
    @SerializedName("headlines") val headlines: List<String>?,
    @SerializedName("total_found") val totalFound: Int?,
    @SerializedName("relevant_count") val relevantCount: Int?
)

/**
 * Hasil analisis sentimen menggunakan model FinBERT.
 */
data class SentimentData(
    @SerializedName("score") val score: Double?,
    @SerializedName("label") val label: String?,
    @SerializedName("breakdown") val breakdown: SentimentBreakdown?,
    @SerializedName("interpretation") val interpretation: String?
)

/**
 * Rincian skor sentimen dari berbagai parameter.
 */
data class SentimentBreakdown(
    @SerializedName("quantitative_score") val quantitativeScore: Double?,
    @SerializedName("finbert_score") val finbertScore: Double?
)

/**
 * Rekomendasi aksi investasi berdasarkan analisis AI.
 */
data class Recommendation(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("priority") val priority: String?
)

/**
 * Narasi analisis mendalam (Executive Summary).
 */
data class AnalysisData(
    @SerializedName("executive_summary") val executiveSummary: String?,
    @SerializedName("quantitative_analysis") val quantitativeAnalysis: QuantitativeAnalysis?,
    @SerializedName("overall") val overall: String?
)

/**
 * Hasil analisis kuantitatif seperti volatilitas.
 */
data class QuantitativeAnalysis(
    @SerializedName("volatility_analysis") val volatilityAnalysis: VolatilityAnalysis?
)

/**
 * Detail analisis volatilitas harga.
 */
data class VolatilityAnalysis(
    @SerializedName("volatility_percent") val volatilityPercent: Double?
)

/**
 * Model data respons untuk Dashboard IHSG.
 * Digunakan untuk memetakan data dari GET /api/ihsg-dashboard.
 */
data class IhsgDashboardResponse(
    @SerializedName("status") val status: String,
    @SerializedName("generated_at") val generatedAt: String,
    @SerializedName("window_days") val windowDays: Int,
    @SerializedName("price_horizon_days") val priceHorizonDays: Int,
    @SerializedName("universe_used") val universeUsed: Int,
    @SerializedName("categories") val categories: Map<String, List<IhsgStockMetric>>?
)

/**
 * Metrik performa saham individu di dalam dashboard.
 */
data class IhsgStockMetric(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("return_pct") val returnPct: Double,
    @SerializedName("volatility_percent") val volatilityPercent: Double,
    @SerializedName("drawdown_pct") val drawdownPct: Double,
    @SerializedName("score") val score: Double
)

