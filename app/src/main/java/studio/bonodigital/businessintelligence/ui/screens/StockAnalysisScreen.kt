package studio.bonodigital.businessintelligence.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.GsonBuilder
import studio.bonodigital.businessintelligence.data.model.BiResponse
import studio.bonodigital.businessintelligence.ui.components.CustomStockChart
import studio.bonodigital.businessintelligence.ui.theme.BearishRed
import studio.bonodigital.businessintelligence.ui.theme.BullishGreen
import studio.bonodigital.businessintelligence.ui.theme.DarkBackground
import studio.bonodigital.businessintelligence.ui.theme.DarkPrimary
import studio.bonodigital.businessintelligence.ui.theme.DarkSurface
import studio.bonodigital.businessintelligence.ui.theme.NeutralYellow
import studio.bonodigital.businessintelligence.ui.theme.TextMuted
import studio.bonodigital.businessintelligence.ui.theme.TextPrimary
import studio.bonodigital.businessintelligence.ui.theme.TextSecondary
import studio.bonodigital.businessintelligence.ui.viewmodel.BiUiState
import studio.bonodigital.businessintelligence.ui.viewmodel.BiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAnalysisScreen(
    viewModel: BiViewModel,
    initialTicker: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val wsStatus by viewModel.wsStatus.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val isFavorited by viewModel.isFavorited.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Picu pencarian awal jika ticker aktif diberikan
    LaunchedEffect(initialTicker) {
        if (initialTicker != null) {
            searchQuery = initialTicker
            viewModel.searchTicker(initialTicker)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analisis Saham AI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Baris Input Pencarian
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Ketik ticker saham (e.g. AAPL)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.searchTicker(searchQuery)
                            keyboardController?.hide()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = DarkPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        viewModel.searchTicker(searchQuery)
                        keyboardController?.hide()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            }

            // Konten Utama
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is BiUiState.Idle -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ketik ticker saham global/lokal di atas untuk memulai analisis cerdas AI.",
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                    is BiUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = DarkPrimary
                        )
                    }
                    is BiUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = BearishRed,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = BearishRed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Button(
                                onClick = { viewModel.searchTicker(searchQuery) },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                    is BiUiState.Success -> {
                        AnalysisResultContent(
                            response = state.response,
                            wsStatus = wsStatus,
                            countdown = countdown,
                            isFavorited = isFavorited,
                            onFavoriteClick = { viewModel.toggleFavorite(state.response.ticker) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisResultContent(
    response: BiResponse,
    wsStatus: String,
    countdown: Int,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit
) {
    val context = LocalContext.current
    val stock = response.data?.stockData
    val sentiment = response.data?.sentiment
    val recommendations = response.data?.recommendations ?: emptyList()
    val news = response.data?.newsData?.headlines ?: emptyList()
    val analysis = response.data?.analysis
    val volatility = analysis?.quantitativeAnalysis?.volatilityAnalysis?.volatilityPercent ?: 0.0

    val sentimentLabel = sentiment?.label ?: "Netral"
    val sentimentScore = sentiment?.score ?: 5.0
    val overallOutlook = analysis?.overall ?: "Neutral"

    val outlookColor = when (overallOutlook) {
        "Bullish" -> BullishGreen
        "Bearish" -> BearishRed
        else -> NeutralYellow
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Ticker
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Analisis Ticker",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = response.ticker,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                // Lencana Outlook
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = outlookColor.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = outlookColor.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = overallOutlook,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = outlookColor
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onFavoriteClick) {
                                Icon(
                                    imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Watchlist",
                                    tint = if (isFavorited) BearishRed else TextSecondary
                                )
                            }
                            IconButton(onClick = {
                                val gson = GsonBuilder().setPrettyPrinting().create()
                                val jsonStr = gson.toJson(response)
                                shareJson(context, response.ticker, jsonStr)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share JSON",
                                    tint = DarkPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Indikator penyegaran otomatis langsung
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val wsColor = when (wsStatus) {
                                "connected" -> BullishGreen
                                "connecting" -> NeutralYellow
                                else -> TextMuted
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(wsColor, shape = RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = "WebSocket: $wsStatus",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        if (wsStatus == "connected" || wsStatus == "idle") {
                            Text(
                                text = "Auto-refresh: ${countdown}s",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Grid Metrik
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (stock != null) {
                        val isUp = (stock.priceChangePercent ?: 0.0) >= 0
                        val changeColor = if (isUp) BullishGreen else BearishRed
                        val changeSym = if (isUp) "+" else ""
                        StatCard(
                            label = "Harga Sekarang",
                            value = String.format("$%.2f", stock.currentPrice),
                            valueColor = TextPrimary,
                            subText = String.format("%s$%.2f (%s%.2f%%)", changeSym, stock.priceChange, changeSym, stock.priceChangePercent),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    StatCard(
                        label = "FinBERT Score",
                        value = String.format("%.1f/10", sentimentScore),
                        valueColor = outlookColor,
                        subText = "Label: $sentimentLabel",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        label = "Volatilitas (7 Hari)",
                        value = String.format("%.2f%%", volatility),
                        valueColor = NeutralYellow,
                        subText = "Rata-rata deviasi harga",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Volume Transaksi",
                        value = stock?.volume?.let { formatVolume(it) } ?: "—",
                        valueColor = DarkPrimary,
                        subText = "Total lembar saham",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Grafik Canvas Kustom
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Grafik Tren Harga (7 Hari Terakhir)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Geser jari di atas grafik untuk melihat nilai spesifik",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (stock != null && !stock.closingPrices.isNullOrEmpty()) {
                        CustomStockChart(
                            closingPrices = stock.closingPrices,
                            dates = stock.dates,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Data harga tidak tersedia", color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Berita CNBC
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Scraping Berita CNBC Terbaru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (news.isNotEmpty()) {
                        news.take(5).forEachIndexed { i, title ->
                            Column {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(6.dp)
                                            .background(DarkPrimary, shape = RoundedCornerShape(3.dp))
                                    )
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (i < 4 && i < news.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        thickness = DividerDefaults.Thickness,
                                        color = Color.White.copy(alpha = 0.05f)
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Tidak ada berita yang ditemukan", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Rekomendasi Strategis
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rekomendasi Strategis AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (recommendations.isNotEmpty()) {
                        recommendations.forEachIndexed { i, rec ->
                            val recColor = when (rec.priority?.lowercase()) {
                                "tinggi" -> BearishRed
                                "sedang" -> NeutralYellow
                                else -> BullishGreen
                            }
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rec.title ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = recColor.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = recColor.copy(alpha = 0.25f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = (rec.priority ?: "rendah").uppercase(),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = recColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rec.description ?: "",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                if (i < recommendations.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        thickness = DividerDefaults.Thickness,
                                        color = Color.White.copy(alpha = 0.05f)
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Rekomendasi tidak tersedia", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Kartu Transparansi dan Eksplanabilitas
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transparansi & Explainability AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Analisis ini diproduksi menggunakan formula bobot: 40% pergerakan harga historis dan 60% analisis sentimen FinBERT terhadap berita keuangan CNBC.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val topPositive = response.data?.topPositiveDrivers ?: emptyList()
                    val topNegative = response.data?.topNegativeDrivers ?: emptyList()

                    if (topPositive.isNotEmpty()) {
                        Text(
                            text = "Driver Positif Utama:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BullishGreen
                        )
                        topPositive.forEach { driver ->
                            Text(
                                text = "• $driver",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (topNegative.isNotEmpty()) {
                        Text(
                            text = "Driver Negatif Utama:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BearishRed
                        )
                        topNegative.forEach { driver ->
                            Text(
                                text = "• $driver",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }
                    }

                    if (analysis?.executiveSummary != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ringkasan Eksekutif:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkPrimary
                        )
                        Text(
                            text = analysis.executiveSummary,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatVolume(volume: Long): String {
    return when {
        volume >= 1_000_000_000 -> String.format("%.2fB", volume.toDouble() / 1_000_000_000)
        volume >= 1_000_000 -> String.format("%.2fM", volume.toDouble() / 1_000_000)
        volume >= 1_000 -> String.format("%.2fK", volume.toDouble() / 1_000)
        else -> volume.toString()
    }
}

private fun shareJson(context: Context, ticker: String, analysisJson: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, analysisJson)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Ekspor Analisis $ticker")
    context.startActivity(shareIntent)
}
