package studio.bonodigital.businessintelligence.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.bonodigital.businessintelligence.ui.theme.*
import studio.bonodigital.businessintelligence.ui.viewmodel.IhsgStockItem
import studio.bonodigital.businessintelligence.ui.viewmodel.IhsgUiState
import studio.bonodigital.businessintelligence.ui.viewmodel.IhsgViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IhsgDashboardScreen(
    viewModel: IhsgViewModel,
    onStockClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lastUpdate by viewModel.lastUpdate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "IHSG Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = lastUpdate?.let {
                                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                "Terakhir diperbarui: ${sdf.format(it)}"
                            } ?: "Memperbarui...",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchIhsgData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = DarkPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is IhsgUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DarkPrimary
                    )
                }
                is IhsgUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = BearishRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.fetchIhsgData() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
                is IhsgUiState.Success -> {
                    IhsgContent(
                        stocks = state.stocks,
                        onStockClick = onStockClick
                    )
                }
            }
        }
    }
}

@Composable
fun IhsgContent(
    stocks: List<IhsgStockItem>,
    onStockClick: (String) -> Unit
) {
    val validStocks = stocks.filter { it.ok }
    val gainers = validStocks.count { it.pct > 0 }
    val losers = validStocks.count { it.pct < 0 }
    
    val avgScore = if (validStocks.isNotEmpty()) {
        validStocks.map { it.score ?: 5.0 }.average()
    } else 5.0

    val overallSentiment = when {
        avgScore >= 6.0 -> "Bullish"
        avgScore >= 4.0 -> "Netral"
        else -> "Bearish"
    }

    val sentColor = when (overallSentiment) {
        "Bullish" -> BullishGreen
        "Bearish" -> BearishRed
        else -> NeutralYellow
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Sentimen Pasar",
                    value = overallSentiment,
                    valueColor = sentColor,
                    subText = "rata-rata IDX",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Rata-rata Skor AI",
                    value = String.format("%.1f/10", avgScore),
                    valueColor = DarkPrimary,
                    subText = "FinBERT Score",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Saham Naik",
                    value = "$gainers",
                    valueColor = BullishGreen,
                    subText = "dari ${validStocks.size} saham",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Saham Turun",
                    value = "$losers",
                    valueColor = BearishRed,
                    subText = "dari ${validStocks.size} saham",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Custom Canvas Chart
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Skor FinBERT — Saham IDX",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Skala 1-10 · hijau ≥ 6 · kuning 4-6 · merah < 4",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    IhsgSentimentBarChart(
                        stocks = validStocks,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }
        }

        // Table List
        item {
            Text(
                text = "IDX Blue Chip Watchlist",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(stocks) { stock ->
            StockListItem(
                stock = stock,
                onClick = { onStockClick(stock.ticker) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    valueColor: Color,
    subText: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subText,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun IhsgSentimentBarChart(
    stocks: List<IhsgStockItem>,
    modifier: Modifier = Modifier
) {
    if (stocks.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = stocks.size
        val gap = 24f
        val totalGaps = gap * (barCount - 1)
        val barWidth = (width - totalGaps) / barCount
        val maxScore = 10f

        stocks.forEachIndexed { index, item ->
            val score = item.score ?: 5.0
            val normalizedHeight = (score / maxScore).toFloat()
            val usableHeight = height - 60f
            val barHeight = usableHeight * normalizedHeight
            
            val x = index * (barWidth + gap)
            val y = height - barHeight - 40f

            val color = when {
                score >= 6.0 -> BullishGreen
                score >= 4.0 -> NeutralYellow
                else -> BearishRed
            }

            // Draw Bar
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )

            // Draw Score Text
            val scoreText = String.format("%.1f", score)
            val scoreLayout = textMeasurer.measure(
                text = scoreText,
                style = TextStyle(
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(
                textMeasurer = textMeasurer,
                text = scoreText,
                style = TextStyle(
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                topLeft = Offset(
                    x + (barWidth - scoreLayout.size.width) / 2,
                    y - scoreLayout.size.height - 4f
                )
            )

            // Draw Ticker Label
            val tickerLayout = textMeasurer.measure(
                text = item.ticker,
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 9.sp
                )
            )
            drawText(
                textMeasurer = textMeasurer,
                text = item.ticker,
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 9.sp
                ),
                topLeft = Offset(
                    x + (barWidth - tickerLayout.size.width) / 2,
                    height - 30f
                )
            )
        }
    }
}

@Composable
fun StockListItem(
    stock: IhsgStockItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = stock.ticker,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = stock.name,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Sector Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = DarkBackground,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stock.sector,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.End
            ) {
                if (stock.ok) {
                    Text(
                        text = String.format("Rp %,.0f", stock.price),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    val isUp = stock.pct >= 0
                    val pctColor = if (isUp) BullishGreen else BearishRed
                    val symbol = if (isUp) "▲" else "▼"
                    Text(
                        text = String.format("%s %.2f%%", symbol, Math.abs(stock.pct)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = pctColor
                    )
                } else {
                    Text(
                        text = "Gagal Memuat",
                        fontSize = 12.sp,
                        color = BearishRed
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                if (stock.ok && stock.score != null) {
                    val label = stock.label ?: "Netral"
                    val labelColor = when {
                        label.lowercase().contains("positif") -> BullishGreen
                        label.lowercase().contains("negatif") -> BearishRed
                        else -> NeutralYellow
                    }
                    Text(
                        text = String.format("Score: %.1f", stock.score),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = labelColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = labelColor.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelColor
                        )
                    }
                } else {
                    Text(
                        text = "—",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
