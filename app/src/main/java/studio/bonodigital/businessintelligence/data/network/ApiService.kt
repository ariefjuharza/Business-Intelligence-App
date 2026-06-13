package studio.bonodigital.businessintelligence.data.network

import retrofit2.http.GET
import retrofit2.http.Query
import studio.bonodigital.businessintelligence.data.model.BiResponse
import studio.bonodigital.businessintelligence.data.model.IhsgDashboardResponse

interface ApiService {
    @GET("/api/bi")
    suspend fun getBiAnalysis(
        @Query("ticker") ticker: String,
        @Query("detail") detail: String = "full",
        @Query("days") days: Int = 7,
        @Query("headline_limit") headlineLimit: Int = 10
    ): BiResponse

    @GET("/api/ihsg-dashboard")
    suspend fun getIhsgDashboard(
        @Query("window_days") windowDays: Int = 30,
        @Query("top_n") topN: Int = 5,
        @Query("price_horizon_days") priceHorizonDays: Int = 200
    ): IhsgDashboardResponse
}
