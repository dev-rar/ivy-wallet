package com.ivy.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.utils.format
import com.ivy.reports.model.CategoryTrendSheetData
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalSet
import java.util.UUID

@Composable
fun BoxScope.CategoryTrendBottomSheet(
    trendData: CategoryTrendSheetData?,
    baseCurrency: String,
    onDismiss: () -> Unit
) {
    if (trendData == null) return

    val contrastColor = findContrastTextColor(trendData.categoryColor)
    val maxSpent = trendData.history.maxOfOrNull { it.spent }?.coerceAtLeast(100.0) ?: 100.0

    val barColor = trendData.categoryColor
    val mediumBg = UI.colors.medium
    val pureBg = UI.colors.pure
    val pureInverseColor = UI.colors.pureInverse

    IvyModal(
        id = UUID.randomUUID(),
        visible = true,
        dismiss = onDismiss,
        PrimaryAction = {
            ModalSet {
                onDismiss()
            }
        }
    ) {
        Spacer(Modifier.height(16.dp))

        // Modal Header with Category Icon and Name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(trendData.categoryColor),
                contentAlignment = Alignment.Center
            ) {
                ItemIconSDefaultIcon(
                    iconName = trendData.categoryIcon,
                    defaultIcon = R.drawable.ic_custom_category_s,
                    tint = contrastColor
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    text = trendData.categoryName,
                    style = UI.typo.b1.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = pureInverseColor
                    )
                )
                Text(
                    text = "Historical Spending Trend",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Stats Row (Average Spent & Peak Month)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avg Monthly Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(UI.shapes.r4)
                    .background(mediumBg)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Monthly Avg",
                    style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${trendData.averageMonthlySpent.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = pureInverseColor
                    )
                )
            }

            // Peak Month Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(UI.shapes.r4)
                    .background(mediumBg)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Highest Month",
                    style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (trendData.highestSpent > 0) "${trendData.highestMonthName.take(3)} (${trendData.highestSpent.format(baseCurrency)})" else "None",
                    style = UI.typo.b2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = Red
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 12-Month Mini Historical Trend Bar Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(UI.shapes.r4)
                .background(pureBg)
                .border(1.5.dp, mediumBg, UI.shapes.r4)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Month-by-Month Spending",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val n = trendData.history.size
                        if (n == 0) return@Canvas

                        val colWidth = size.width / n.toFloat()
                        val barWidth = (colWidth * 0.5f).coerceIn(4.dp.toPx(), 14.dp.toPx())
                        val chartHeight = size.height - 20.dp.toPx()

                        // Reference baseline
                        drawLine(
                            color = mediumBg,
                            start = Offset(0f, chartHeight),
                            end = Offset(size.width, chartHeight),
                            strokeWidth = 1.dp.toPx()
                        )

                        trendData.history.forEachIndexed { i, point ->
                            val colCenter = (i + 0.5f) * colWidth
                            val barH = ((point.spent / maxSpent) * (chartHeight - 4.dp.toPx())).toFloat()
                                .coerceAtLeast(if (point.spent > 0) 3.dp.toPx() else 0f)

                            if (barH > 0) {
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(colCenter - barWidth / 2f, chartHeight - barH),
                                    size = Size(barWidth, barH),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }
                    }

                    // Month Labels below chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        trendData.history.forEach { pt ->
                            Text(
                                text = pt.monthAbbrev.take(1),
                                style = UI.typo.c.style(
                                    color = Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}
