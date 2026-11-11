package com.ivy.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.reports.model.MonthlyReportData
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red

@Composable
fun MonthlyAreaChart(
    monthlyData: List<MonthlyReportData>,
    selectedMonthIndex: Int,
    currency: String,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (monthlyData.isEmpty()) return

    val maxAmount = monthlyData.maxOfOrNull { maxOf(it.income, it.expenses) }
        ?.coerceAtLeast(100.0) ?: 100.0

    val currentMonthData = monthlyData.getOrNull(selectedMonthIndex) ?: monthlyData.first()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.pure)
            .padding(16.dp)
    ) {
        // Header with Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cash Flow Trend",
                style = UI.typo.b2.style(
                    fontWeight = FontWeight.Bold,
                    color = UI.colors.pureInverse
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Green)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Income",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Red)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Expenses",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Interactive Month Scrubber Preview Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(UI.colors.medium.copy(alpha = 0.5f))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMonthData.monthName,
                    style = UI.typo.b2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+$currency ${String.format("%,.0f", currentMonthData.income)}",
                        style = UI.typo.c.style(
                            fontWeight = FontWeight.Bold,
                            color = Green
                        )
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "-$currency ${String.format("%,.0f", currentMonthData.expenses)}",
                        style = UI.typo.c.style(
                            fontWeight = FontWeight.Bold,
                            color = Red
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Smooth Area Chart Canvas
        val highlightColor = UI.colors.pureInverse
        val gridColor = UI.colors.medium
        val pureColor = UI.colors.pure

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(monthlyData.size) {
                        detectTapGestures { offset ->
                            val colWidth = size.width / (monthlyData.size - 1).coerceAtLeast(1).toFloat()
                            val index = ((offset.x + colWidth / 2f) / colWidth).toInt()
                                .coerceIn(0, monthlyData.size - 1)
                            onMonthSelected(index)
                        }
                    }
                    .pointerInput(monthlyData.size) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val colWidth = size.width / (monthlyData.size - 1).coerceAtLeast(1).toFloat()
                                val index = ((offset.x + colWidth / 2f) / colWidth).toInt()
                                    .coerceIn(0, monthlyData.size - 1)
                                onMonthSelected(index)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val colWidth = size.width / (monthlyData.size - 1).coerceAtLeast(1).toFloat()
                                val index = ((change.position.x + colWidth / 2f) / colWidth).toInt()
                                    .coerceIn(0, monthlyData.size - 1)
                                onMonthSelected(index)
                            }
                        )
                    }
            ) {
                val n = monthlyData.size
                if (n < 2) return@Canvas

                val stepX = size.width / (n - 1).toFloat()
                val topPadding = 12.dp.toPx()
                val bottomPadding = 6.dp.toPx()
                val availableHeight = size.height - topPadding - bottomPadding

                fun getY(value: Double): Float {
                    val ratio = (value / maxAmount).toFloat().coerceIn(0f, 1f)
                    return topPadding + availableHeight * (1f - ratio)
                }

                // Draw background horizontal reference lines
                val numGuides = 3
                for (i in 1..numGuides) {
                    val y = topPadding + availableHeight * (i.toFloat() / (numGuides + 1))
                    drawLine(
                        color = gridColor.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Compute points for Income and Expenses
                val incomePoints = monthlyData.mapIndexed { i, d -> Offset(i * stepX, getY(d.income)) }
                val expensePoints = monthlyData.mapIndexed { i, d -> Offset(i * stepX, getY(d.expenses)) }

                // Helper to create smooth cubic bezier curve path
                fun createSmoothPath(points: List<Offset>): Path {
                    val path = Path()
                    if (points.isEmpty()) return path
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                        val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                        path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                    }
                    return path
                }

                // 1. Draw Income Gradient Area and Line
                val incomeCurve = createSmoothPath(incomePoints)
                val incomeArea = Path().apply {
                    addPath(incomeCurve)
                    lineTo(size.width, size.height - bottomPadding)
                    lineTo(0f, size.height - bottomPadding)
                    close()
                }
                drawPath(
                    path = incomeArea,
                    brush = Brush.verticalGradient(
                        colors = listOf(Green.copy(alpha = 0.35f), Color.Transparent),
                        startY = topPadding,
                        endY = size.height
                    )
                )
                drawPath(
                    path = incomeCurve,
                    color = Green,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 2. Draw Expense Gradient Area and Line
                val expenseCurve = createSmoothPath(expensePoints)
                val expenseArea = Path().apply {
                    addPath(expenseCurve)
                    lineTo(size.width, size.height - bottomPadding)
                    lineTo(0f, size.height - bottomPadding)
                    close()
                }
                drawPath(
                    path = expenseArea,
                    brush = Brush.verticalGradient(
                        colors = listOf(Red.copy(alpha = 0.28f), Color.Transparent),
                        startY = topPadding,
                        endY = size.height
                    )
                )
                drawPath(
                    path = expenseCurve,
                    color = Red,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 3. Draw Vertical Scrubber Line & Highlight Dots at Selected Month
                if (selectedMonthIndex in 0 until n) {
                    val scrubberX = selectedMonthIndex * stepX
                    drawLine(
                        color = highlightColor.copy(alpha = 0.45f),
                        start = Offset(scrubberX, 0f),
                        end = Offset(scrubberX, size.height),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val selIncomeY = incomePoints[selectedMonthIndex].y
                    val selExpenseY = expensePoints[selectedMonthIndex].y

                    // Income Dot
                    drawCircle(
                        color = pureColor,
                        radius = 6.dp.toPx(),
                        center = Offset(scrubberX, selIncomeY)
                    )
                    drawCircle(
                        color = Green,
                        radius = 4.dp.toPx(),
                        center = Offset(scrubberX, selIncomeY)
                    )

                    // Expense Dot
                    drawCircle(
                        color = pureColor,
                        radius = 6.dp.toPx(),
                        center = Offset(scrubberX, selExpenseY)
                    )
                    drawCircle(
                        color = Red,
                        radius = 4.dp.toPx(),
                        center = Offset(scrubberX, selExpenseY)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Clean Quarterly X-Axis Labels (Jan, Apr, Jul, Oct, Dec) - not crowding all 12 labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val keyIndices = listOf(0, 3, 6, 9, 11) // Jan, Apr, Jul, Oct, Dec
            keyIndices.forEach { index ->
                if (index < monthlyData.size) {
                    val isNearSelected = selectedMonthIndex == index
                    Text(
                        text = monthlyData[index].monthAbbrev.take(3),
                        style = UI.typo.c.style(
                            color = if (isNearSelected) UI.colors.pureInverse else Gray,
                            fontWeight = if (isNearSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}
