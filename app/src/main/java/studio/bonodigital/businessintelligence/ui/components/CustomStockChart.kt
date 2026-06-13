package studio.bonodigital.businessintelligence.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.bonodigital.businessintelligence.ui.theme.BullishGreen
import studio.bonodigital.businessintelligence.ui.theme.TextMuted
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun CustomStockChart(
    closingPrices: List<Double>,
    dates: List<String>?,
    modifier: Modifier = Modifier
) {
    if (closingPrices.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf(-1) }
    var touchX by remember { mutableStateOf(0f) }

    val maxPrice = closingPrices.maxOrNull() ?: 1.0
    val minPrice = closingPrices.minOrNull() ?: 0.0
    val priceRange = if (maxPrice == minPrice) 1.0 else maxPrice - minPrice

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width
                            val step = width / (closingPrices.size - 1).coerceAtLeast(1)
                            selectedIndex = (offset.x / step).roundToInt().coerceIn(0, closingPrices.size - 1)
                            touchX = offset.x
                        },
                        onDrag = { change, _ ->
                            val width = size.width
                            val step = width / (closingPrices.size - 1).coerceAtLeast(1)
                            selectedIndex = (change.position.x / step).roundToInt().coerceIn(0, closingPrices.size - 1)
                            touchX = change.position.x
                        },
                        onDragEnd = {
                            selectedIndex = -1
                        },
                        onDragCancel = {
                            selectedIndex = -1
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val pointsCount = closingPrices.size
            val stepX = width / (pointsCount - 1).coerceAtLeast(1)

            // Draw Grid lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height * i / gridLines
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw Y axis labels
                val labelPrice = maxPrice - (priceRange * i / gridLines)
                val labelText = String.format("%.2f", labelPrice)
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    style = TextStyle(color = TextMuted, fontSize = 9.sp),
                    topLeft = Offset(8f, y - 16f)
                )
            }

            // Draw line chart path
            val path = Path()
            val points = mutableListOf<Offset>()
            closingPrices.forEachIndexed { index, price ->
                val x = index * stepX
                val normalizedY = (price - minPrice) / priceRange
                val y = height - (normalizedY * (height - 40f) + 20f).toFloat() // padding 20f top/bottom
                points.add(Offset(x, y))
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    // Draw smooth curve
                    val prevPoint = points[index - 1]
                    val controlX = (prevPoint.x + x) / 2
                    path.cubicTo(controlX, prevPoint.y, controlX, y, x, y)
                }
            }

            // Draw line
            drawPath(
                path = path,
                color = BullishGreen,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw gradient area under line
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BullishGreen.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw dots at points
            points.forEach { point ->
                drawCircle(
                    color = BullishGreen,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }

            // Draw touch indicator
            if (selectedIndex != -1 && selectedIndex < points.size) {
                val point = points[selectedIndex]
                
                // Vertical line
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(point.x, 0f),
                    end = Offset(point.x, height),
                    strokeWidth = 1.dp.toPx()
                )

                // Hover dot
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = BullishGreen,
                    radius = 8.dp.toPx(),
                    center = point,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Tooltip text
                val dateStr = dates?.getOrNull(selectedIndex) ?: ""
                val priceVal = closingPrices[selectedIndex]
                val tooltipText = "$dateStr: \$${String.format("%.2f", priceVal)}"
                val textLayoutResult = textMeasurer.measure(
                    text = tooltipText,
                    style = TextStyle(color = Color.White, fontSize = 10.sp)
                )
                val tooltipWidth = textLayoutResult.size.width
                val tooltipX = (point.x - tooltipWidth / 2).coerceIn(10f, width - tooltipWidth - 10f)
                val tooltipY = (point.y - 40f).coerceIn(10f, height - 30f)

                // Tooltip background
                drawRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(tooltipX - 8f, tooltipY - 4f),
                    size = Size(
                        tooltipWidth + 16f,
                        textLayoutResult.size.height + 8f
                    )
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = tooltipText,
                    style = TextStyle(color = Color.White, fontSize = 10.sp),
                    topLeft = Offset(tooltipX, tooltipY)
                )
            }
        }
    }
}
