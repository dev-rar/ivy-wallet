package com.ivy.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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
fun MonthlyBarChart(
    monthlyData: List<MonthlyReportData>,
    selectedMonthIndex: Int,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (monthlyData.isEmpty()) return

    val maxAmount = monthlyData.maxOfOrNull { maxOf(it.income, it.expenses) }?.coerceAtLeast(1.0) ?: 1.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.pure)
            .padding(16.dp)
    ) {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Cash Flow",
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

        Spacer(Modifier.height(16.dp))

        // Chart Canvas
        val highlightColor = UI.colors.medium
        val incomeColor = Green
        val expenseColor = Red

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(monthlyData.size) {
                        detectTapGestures { offset ->
                            val colWidth = size.width / monthlyData.size.toFloat()
                            val index = (offset.x / colWidth).toInt().coerceIn(0, monthlyData.size - 1)
                            onMonthSelected(index)
                        }
                    }
            ) {
                val numMonths = monthlyData.size
                val colWidth = size.width / numMonths.toFloat()
                val bottomLabelHeight = 24.dp.toPx()
                val chartHeight = size.height - bottomLabelHeight
                val barWidth = (colWidth * 0.28f).coerceIn(4.dp.toPx(), 12.dp.toPx())
                val barSpacing = 2.dp.toPx()
                val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

                // Draw horizontal guide lines
                val numGuides = 3
                for (i in 1..numGuides) {
                    val y = chartHeight * (i.toFloat() / (numGuides + 1))
                    drawLine(
                        color = highlightColor.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                monthlyData.forEachIndexed { index, data ->
                    val colCenter = (index + 0.5f) * colWidth

                    // Draw selected month highlight background
                    if (index == selectedMonthIndex) {
                        drawRoundRect(
                            color = highlightColor,
                            topLeft = Offset(index * colWidth + 2.dp.toPx(), 0f),
                            size = Size(colWidth - 4.dp.toPx(), size.height),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }

                    // Bar heights
                    val incomeHeight = ((data.income / maxAmount) * (chartHeight - 8.dp.toPx())).toFloat()
                        .coerceAtLeast(if (data.income > 0) 4.dp.toPx() else 0f)
                    val expenseHeight = ((data.expenses / maxAmount) * (chartHeight - 8.dp.toPx())).toFloat()
                        .coerceAtLeast(if (data.expenses > 0) 4.dp.toPx() else 0f)

                    // Draw Income Bar (Left)
                    if (incomeHeight > 0) {
                        val incomeLeft = colCenter - barWidth - (barSpacing / 2f)
                        val incomeTop = chartHeight - incomeHeight
                        val path = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    left = incomeLeft,
                                    top = incomeTop,
                                    right = incomeLeft + barWidth,
                                    bottom = chartHeight,
                                    topLeftCornerRadius = cornerRadius,
                                    topRightCornerRadius = cornerRadius,
                                    bottomLeftCornerRadius = CornerRadius.Zero,
                                    bottomRightCornerRadius = CornerRadius.Zero
                                )
                            )
                        }
                        drawPath(path, incomeColor)
                    }

                    // Draw Expense Bar (Right)
                    if (expenseHeight > 0) {
                        val expenseLeft = colCenter + (barSpacing / 2f)
                        val expenseTop = chartHeight - expenseHeight
                        val path = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    left = expenseLeft,
                                    top = expenseTop,
                                    right = expenseLeft + barWidth,
                                    bottom = chartHeight,
                                    topLeftCornerRadius = cornerRadius,
                                    topRightCornerRadius = cornerRadius,
                                    bottomLeftCornerRadius = CornerRadius.Zero,
                                    bottomRightCornerRadius = CornerRadius.Zero
                                )
                            )
                        }
                        drawPath(path, expenseColor)
                    }
                }
            }

            // Month Labels Row below the chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                monthlyData.forEachIndexed { index, data ->
                    val isSelected = index == selectedMonthIndex
                    Text(
                        text = data.monthAbbrev.take(3),
                        style = UI.typo.c.style(
                            color = if (isSelected) UI.colors.pureInverse else Gray,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
