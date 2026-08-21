package com.antigravity.expensetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.expensetracker.domain.model.DailySpending
import com.antigravity.expensetracker.ui.theme.Emerald40
import com.antigravity.expensetracker.ui.theme.Emerald80
import com.antigravity.expensetracker.ui.theme.SpendingRed
import com.antigravity.expensetracker.ui.theme.WarningOrange
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpendingBarChart(
    weeklyTrend: List<DailySpending>,
    dailyLimit: Double = 1200.0,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(weeklyTrend) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    val maxDailyAmount = remember(weeklyTrend, dailyLimit) {
        val maxSpend = weeklyTrend.maxOfOrNull { it.totalAmount } ?: 0.0
        max(maxSpend, dailyLimit * 1.3).coerceAtLeast(2000.0)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                RoundedCornerShape(22.dp)
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "7-Day Spending Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap a bar to inspect category split",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(WarningOrange)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Cap: ${currencyFormat.format(dailyLimit)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .pointerInput(weeklyTrend) {
                            detectTapGestures { offset ->
                                if (weeklyTrend.isEmpty()) return@detectTapGestures
                                val barSlotWidth = size.width / weeklyTrend.size
                                val tappedIndex = (offset.x / barSlotWidth).toInt().coerceIn(0, weeklyTrend.size - 1)
                                selectedIndex = if (selectedIndex == tappedIndex) null else tappedIndex
                            }
                        }
                ) {
                    val count = weeklyTrend.size
                    if (count == 0) return@Canvas

                    val availableHeight = size.height - 32.dp.toPx()
                    val barSlotWidth = size.width / count
                    val barWidth = barSlotWidth * 0.44f

                    // Draw daily limit dashed reference line
                    val limitY = availableHeight * (1f - (dailyLimit / maxDailyAmount).toFloat())
                    drawLine(
                        color = WarningOrange.copy(alpha = 0.45f),
                        start = Offset(0f, limitY),
                        end = Offset(size.width, limitY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )

                    // Draw Bars
                    weeklyTrend.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        val isExceeded = item.totalAmount > dailyLimit

                        val barHeight = (availableHeight * (item.totalAmount / maxDailyAmount).toFloat() * animationProgress.value)
                            .coerceAtLeast(if (item.totalAmount > 0) 8.dp.toPx() else 2.dp.toPx())

                        val startX = index * barSlotWidth + (barSlotWidth - barWidth) / 2f
                        val topY = availableHeight - barHeight

                        val barColor = when {
                            isSelected -> if (isExceeded) SpendingRed else (if (isDark) Emerald80 else Emerald40)
                            isExceeded -> SpendingRed.copy(alpha = 0.85f)
                            item.isToday -> primaryColor
                            item.totalAmount == 0.0 -> surfaceVariantColor.copy(alpha = 0.35f)
                            else -> primaryColor.copy(alpha = 0.60f)
                        }

                        // Bar background track
                        drawRoundRect(
                            color = surfaceVariantColor.copy(alpha = if (item.isWeekend) 0.25f else 0.15f),
                            topLeft = Offset(startX, 0f),
                            size = Size(barWidth, availableHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Active Bar
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(startX, topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Draw X-axis Day labels
                        drawIntoCanvas { canvas ->
                            val paint = android.graphics.Paint().apply {
                                color = if (item.isToday) primaryColor.toArgb() else textMutedColor
                                textSize = 11.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = item.isToday || isSelected
                                isAntiAlias = true
                            }
                            canvas.nativeCanvas.drawText(
                                if (item.isToday) "Today" else item.dayLabel,
                                startX + barWidth / 2f,
                                size.height - 6.dp.toPx(),
                                paint
                            )
                        }
                    }
                }
            }

            // Interactive Bar Breakdown Overlay
            AnimatedVisibility(
                visible = selectedIndex != null && selectedIndex!! < weeklyTrend.size,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                selectedIndex?.let { idx ->
                    val selectedItem = weeklyTrend[idx]
                    val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM")
                    val isExceeded = selectedItem.totalAmount > dailyLimit

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedItem.date.format(dateFormatter),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currencyFormat.format(selectedItem.totalAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isExceeded) SpendingRed else MaterialTheme.colorScheme.primary
                            )
                        }

                        if (selectedItem.categoryBreakdown.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                selectedItem.categoryBreakdown.forEach { (cat, amount) ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(cat.getColor().copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(cat.getColor())
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "${cat.displayName}: ${currencyFormat.format(amount)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Zero spend recorded on this date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
