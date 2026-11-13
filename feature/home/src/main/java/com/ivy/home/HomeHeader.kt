package com.ivy.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.model.TransactionType
import com.ivy.design.api.LocalTimeConverter
import com.ivy.design.api.LocalTimeFormatter
import com.ivy.design.api.LocalTimeProvider
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.data.model.TimePeriod
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.ui.component.transaction.TransactionsDividerLine
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.horizontalSwipeListener
import com.ivy.legacy.utils.isNotNullOrBlank
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.rememberSwipeListenerState
import com.ivy.legacy.utils.springBounce
import com.ivy.legacy.utils.verticalSwipeListener
import com.ivy.navigation.PieChartStatisticScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.BalanceRow
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.components.IvyOutlinedButton
import kotlin.math.absoluteValue

@ExperimentalAnimationApi
@Composable
internal fun HomeHeader(
    expanded: Boolean,
    name: String,
    period: TimePeriod,
    currency: String,
    balance: Double,
    onShowMonthModal: () -> Unit,
    onBalanceClick: () -> Unit,
    onSelectNextMonth: () -> Unit,
    hideBalance: Boolean,
    onHiddenBalanceClick: () -> Unit,
    onSelectPreviousMonth: () -> Unit,
) {
    Column {
        val percentExpanded by animateFloatAsState(
            targetValue = if (expanded) 1f else 0f,
            animationSpec = springBounce(
                stiffness = Spring.StiffnessLow
            ),
            label = "Home Header Expand Collapse"
        )

        Spacer(Modifier.height(20.dp))

        HeaderStickyRow(
            percentExpanded = percentExpanded,
            name = name,
            period = period,
            currency = currency,
            balance = balance,
            hideBalance = hideBalance,

            onShowMonthModal = onShowMonthModal,
            onBalanceClick = onBalanceClick,
            onHiddenBalanceClick = onHiddenBalanceClick,
            onSelectNextMonth = onSelectNextMonth,
            onSelectPreviousMonth = onSelectPreviousMonth,
        )

        Spacer(Modifier.height(16.dp))

        if (percentExpanded < 0.5f) {
            TransactionsDividerLine(
                modifier = Modifier.alpha(1f - percentExpanded),
                paddingHorizontal = 0.dp
            )
        }
    }
}

@Composable
private fun HeaderStickyRow(
    percentExpanded: Float,
    name: String,
    period: TimePeriod,
    currency: String,
    balance: Double,
    onShowMonthModal: () -> Unit,
    onBalanceClick: () -> Unit,
    onSelectNextMonth: () -> Unit,
    hideBalance: Boolean,
    onHiddenBalanceClick: () -> Unit,
    onSelectPreviousMonth: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                modifier = Modifier
                    .testTag("home_greeting_text"),
                text = if (name.isNotNullOrBlank()) {
                    stringResource(
                        R.string.hi_name,
                        name,
                    )
                } else {
                    stringResource(R.string.hi)
                },
                style = UI.typo.b1.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse,
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }

        IvyOutlinedButton(
            modifier = Modifier.horizontalSwipeListener(
                sensitivity = 75,
                state = rememberSwipeListenerState(),
                onSwipeLeft = {
                    onSelectNextMonth()
                },
                onSwipeRight = {
                    onSelectPreviousMonth()
                },
            ),
            iconStart = R.drawable.ic_calendar,
            text = period.toDisplayShort(
                startDateOfMonth = ivyWalletCtx().startDayOfMonth,
                timeConverter = LocalTimeConverter.current,
                timeProvider = LocalTimeProvider.current,
                timeFormatter = LocalTimeFormatter.current,
            ),
            minWidth = 130.dp,
        ) {
            onShowMonthModal()
        }

        Spacer(Modifier.width(12.dp))

        Spacer(Modifier.width(40.dp)) // settings menu button spacer
    }
}

@Suppress("UnusedParameter")
@ExperimentalAnimationApi
@Composable
fun CashFlowInfo(
    currency: String,
    balance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    hideBalance: Boolean,
    hideIncome: Boolean,
    onHiddenIncomeClick: () -> Unit,
    onOpenMoreMenu: () -> Unit,
    onBalanceClick: () -> Unit,
    percentExpanded: Float,
    onHiddenBalanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cashflow = monthlyIncome - monthlyExpenses
    val isPositiveCashflow = cashflow >= 0
    val savingsRate = if (monthlyIncome > 0) ((cashflow / monthlyIncome) * 100.0).coerceAtLeast(0.0) else 0.0
    val nav = navigation()

    Column(
        modifier = modifier
            .verticalSwipeListener(
                sensitivity = Constants.SWIPE_DOWN_THRESHOLD_OPEN_MORE_MENU,
                state = rememberSwipeListenerState(),
                onSwipeDown = {
                    onOpenMoreMenu()
                },
            ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(UI.shapes.r4)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1B1D24),
                            Color(0xFF2A2E39)
                        )
                    )
                )
                .border(1.dp, White.copy(alpha = 0.12f), UI.shapes.r4)
        ) {
            // Top Row: EMV Credit Card Chip + "TOTAL BALANCE" label on Left | Cashflow Pill on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Metallic EMV Chip
                    Box(
                        modifier = Modifier
                            .size(width = 34.dp, height = 24.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE6C365),
                                        Color(0xFFC49B38)
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFFFFE28A).copy(alpha = 0.6f), RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 18.dp, height = 12.dp)
                                .border(1.dp, Color(0xFF8C6B1F).copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "CASH FLOW",
                        style = UI.typo.c.style(
                            color = White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                if (!hideBalance && monthlyIncome > 0) {
                    Box(
                        modifier = Modifier
                            .clip(UI.shapes.rFull)
                            .background(
                                if (isPositiveCashflow) Green.copy(alpha = 0.18f) else Red.copy(alpha = 0.18f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${String.format("%.1f", savingsRate)}% Saved",
                            style = UI.typo.nC.style(
                                color = if (isPositiveCashflow) Green else Red,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Center: Cash Flow Row
            BalanceRow(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clickableNoIndication(rememberInteractionSource()) {
                        if (hideBalance) {
                            onHiddenBalanceClick()
                        } else {
                            onBalanceClick()
                        }
                    }
                    .testTag("home_balance"),
                textColor = White,
                currency = currency,
                balance = cashflow,
                balanceFontSize = 24.sp,
                currencyFontSize = 20.sp,
                shortenBigNumbers = false,
                hiddenMode = hideBalance
            )

            Spacer(Modifier.height(20.dp))

            // Bottom Strip: Unified Income & Expenses Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White.copy(alpha = 0.06f))
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income Section
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(UI.shapes.r3)
                        .testTag("home_card_income")
                        .clickable {
                            if (hideIncome) {
                                onHiddenIncomeClick()
                            } else {
                                nav.navigateTo(
                                    PieChartStatisticScreen(
                                        type = TransactionType.INCOME,
                                    )
                                )
                            }
                        }
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Green.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IvyIcon(
                            icon = R.drawable.ic_income_white,
                            tint = Green,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.income),
                            style = UI.typo.c.style(
                                color = White.copy(alpha = 0.65f),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        if (hideIncome) {
                            Text(
                                text = "****",
                                style = UI.typo.nB2.style(
                                    color = White,
                                    fontWeight = FontWeight.ExtraBold
                                ).copy(fontSize = 13.sp)
                            )
                        } else {
                            Text(
                                text = "${monthlyIncome.format(currency)} $currency",
                                style = UI.typo.nB2.style(
                                    color = Green,
                                    fontWeight = FontWeight.ExtraBold
                                ).copy(fontSize = 13.sp),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Vertical Divider
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(White.copy(alpha = 0.14f))
                )

                Spacer(Modifier.width(6.dp))

                // Expense Section
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(UI.shapes.r3)
                        .testTag("home_card_expense")
                        .clickable {
                            nav.navigateTo(
                                PieChartStatisticScreen(
                                    type = TransactionType.EXPENSE,
                                )
                            )
                        }
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Red.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IvyIcon(
                            icon = R.drawable.ic_expense,
                            tint = Red,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.expenses),
                            style = UI.typo.c.style(
                                color = White.copy(alpha = 0.65f),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${monthlyExpenses.absoluteValue.format(currency)} $currency",
                            style = UI.typo.nB2.style(
                                color = White,
                                fontWeight = FontWeight.ExtraBold
                            ).copy(fontSize = 13.sp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
