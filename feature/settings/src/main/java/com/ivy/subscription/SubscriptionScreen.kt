package com.ivy.subscription

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.cloud.CashPulseCloudAccountManager
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.IvyWalletPreview
import com.ivy.navigation.SubscriptionScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red3
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.IvyToolbar
import kotlinx.coroutines.delay

private enum class SubscriptionTierOption {
    FREE,
    PAID
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxWithConstraintsScope.SubscriptionScreenImpl(
    screen: SubscriptionScreen
) {
    val viewModel: SubscriptionViewModel = screenScopedViewModel()
    val state = viewModel.uiState()

    // Middleware guard: Paid users cannot access the subscription page or revert to Free
    LaunchedEffect(screen.launchedFromOnboarding, state.isPro) {
        viewModel.bindLaunchContext(screen.launchedFromOnboarding)
        if (state.isPro) {
            viewModel.redirectPaidUser()
        }
    }
    if (state.isPro) return

    val activity = LocalContext.current as? Activity

    SubscriptionScreenUi(
        state = state,
        launchedFromOnboarding = screen.launchedFromOnboarding,
        onSendOtp = { phone ->
            viewModel.onEvent(SubscriptionEvent.SendMobileOtp(activity, phone))
        },
        onVerifyOtp = { phone, otp ->
            viewModel.onEvent(
                SubscriptionEvent.VerifyMobileOtp(
                    phoneNumber = phone,
                    otpCode = otp,
                    launchedFromOnboarding = screen.launchedFromOnboarding
                )
            )
        },
        onResetOtpStep = { viewModel.onEvent(SubscriptionEvent.ResetOtpStep) },
        onUnlockPaidTier = {
            viewModel.onEvent(
                SubscriptionEvent.SelectPlan(
                    activity = activity,
                    planId = CashPulseCloudAccountManager.PLAN_LIFETIME
                )
            )
        },
        onSignOutOnline = { viewModel.onEvent(SubscriptionEvent.SignOutOnline) },
        onContinueToWalletSetup = { viewModel.onEvent(SubscriptionEvent.ContinueToWalletSetup) },
        onClearMessage = { viewModel.onEvent(SubscriptionEvent.ClearMessage) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxWithConstraintsScope.SubscriptionScreenUi(
    state: SubscriptionState,
    launchedFromOnboarding: Boolean,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
    onResetOtpStep: () -> Unit,
    onUnlockPaidTier: () -> Unit,
    onSignOutOnline: () -> Unit,
    onContinueToWalletSetup: () -> Unit,
    onClearMessage: () -> Unit
) {
    val nav = navigation()
    val defaultCountry = remember { defaultCountryForLocale() }
    val initialSplit = remember(state.phoneNumber) {
        splitE164Phone(state.phoneNumber, defaultCountry)
    }
    var selectedCountry by remember(state.phoneNumber) { mutableStateOf(initialSplit.first) }
    var localPhoneDigits by remember(state.phoneNumber) { mutableStateOf(initialSplit.second) }
    var otpInput by remember { mutableStateOf("") }
    var showCountryPicker by remember { mutableStateOf(false) }
    var selectedTier by remember { mutableStateOf(SubscriptionTierOption.FREE) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        stickyHeader {
            IvyToolbar(onBack = { nav.onBackPressed() }) {
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.drawable.cashpulse_brand_emblem),
                    contentDescription = "CashPulse Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Subscription",
                    style = UI.typo.b2.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.width(24.dp))
            }
        }

        item {
            Spacer(Modifier.height(10.dp))

            if (!state.statusMessage.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Green.copy(alpha = 0.14f))
                        .border(1.dp, Green.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { onClearMessage() }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.statusMessage,
                        style = UI.typo.c.style(color = Green, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            // First Card: Mobile Number Verification
            OnlineAccountVerificationCard(
                state = state,
                selectedCountry = selectedCountry,
                localPhoneDigits = localPhoneDigits,
                onOpenCountryPicker = { showCountryPicker = true },
                onLocalPhoneChange = { digits ->
                    localPhoneDigits = digits.filter { it.isDigit() }.take(15)
                },
                otpInput = otpInput,
                onOtpChange = { code ->
                    otpInput = code.filter { it.isDigit() }.take(6)
                },
                onSendOtp = {
                    val fullNumber = buildE164Phone(selectedCountry, localPhoneDigits)
                    onSendOtp(fullNumber)
                },
                onVerifyOtp = {
                    val fullNumber = buildE164Phone(selectedCountry, localPhoneDigits)
                    onVerifyOtp(fullNumber, otpInput)
                },
                onChangeNumber = {
                    otpInput = ""
                    onResetOtpStep()
                },
                onSignOutOnline = onSignOutOnline
            )

            Spacer(Modifier.height(22.dp))

            // Second Section: Membership Tiers (Free Tier selected by default + Paid Tier)
            MembershipTiersSection(
                selectedTier = selectedTier,
                launchedFromOnboarding = launchedFromOnboarding,
                onSelectTier = { selectedTier = it },
                onUnlockPaidTier = onUnlockPaidTier,
                onContinueWithFreeTier = onContinueToWalletSetup
            )

            if (launchedFromOnboarding && selectedTier == SubscriptionTierOption.PAID) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onContinueToWalletSetup) {
                        Text(
                            text = if (state.isPhoneVerified) {
                                "Continue to Wallet Setup"
                            } else {
                                "Continue with Offline Account instead"
                            },
                            color = Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(64.dp))
        }
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            selectedCountry = selectedCountry,
            onSelectCountry = { country ->
                selectedCountry = country
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }
}

@Composable
private fun FeatureCheckRow(
    text: String,
    included: Boolean = true
) {
    val badgeColor = if (included) Green else White.copy(alpha = 0.25f)
    val symbolColor = if (included) Green else White.copy(alpha = 0.6f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = if (included) 0.2f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (included) "✓" else "✕",
                color = symbolColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = UI.typo.c.style(
                color = White.copy(alpha = if (included) 0.85f else 0.55f),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun OnlineAccountVerificationCard(
    state: SubscriptionState,
    selectedCountry: CountryPhoneCode,
    localPhoneDigits: String,
    onOpenCountryPicker: () -> Unit,
    onLocalPhoneChange: (String) -> Unit,
    otpInput: String,
    onOtpChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onChangeNumber: () -> Unit,
    onSignOutOnline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(colors = listOf(Color(0xFF171C28), Color(0xFF121620)))
            )
            .border(1.dp, Green.copy(alpha = 0.32f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "ONLINE ACCOUNT • MOBILE VERIFICATION",
            style = UI.typo.nC.style(color = Green, fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (state.isPhoneVerified) {
                "Verified Account"
            } else if (state.otpSent) {
                "Enter Verification Code"
            } else {
                "Verify your mobile number"
            },
            style = UI.typo.b1.style(color = White, fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (state.isPhoneVerified) {
                "Your mobile number secures your account and lets you restore your cloud backup even after reinstalling."
            } else if (state.otpSent) {
                "Enter the 6-digit code sent to ${selectedCountry.dialCode} $localPhoneDigits."
            } else {
                "Enter your mobile number to check for an existing cloud backup or link your account."
            },
            style = UI.typo.c.style(color = White.copy(alpha = 0.72f), fontWeight = FontWeight.Medium)
        )

        Spacer(Modifier.height(14.dp))

        when {
            state.isPhoneVerified -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(White.copy(alpha = 0.06f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.phoneNumber,
                        style = UI.typo.nB1.style(color = White, fontWeight = FontWeight.ExtraBold)
                    )
                    Box(
                        modifier = Modifier
                            .clip(UI.shapes.rFull)
                            .background(Green.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "VERIFIED ✓",
                            style = UI.typo.nC.style(color = Green, fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = if (state.lastCloudSync != "Never") {
                        "Last backup: ${state.lastCloudSync}"
                    } else {
                        "Select Paid Tier below to enable daily end-of-day cloud backup."
                    },
                    style = UI.typo.c.style(
                        color = White.copy(alpha = 0.68f),
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onSignOutOnline() }
                        .padding(top = 4.dp),
                    text = "Sign out online account",
                    style = UI.typo.c.style(color = Red3, fontWeight = FontWeight.Bold)
                )
            }

            state.otpSent -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        modifier = Modifier.clickable { onChangeNumber() },
                        text = "Change number",
                        style = UI.typo.c.style(color = Green, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(10.dp))

                OtpPinInput(
                    otp = otpInput,
                    onOtpChange = onOtpChange,
                    onComplete = { if (!state.isLoading) onVerifyOtp() }
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = otpInput.length == 6 && !state.isLoading,
                    onClick = onVerifyOtp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = White,
                        disabledContainerColor = Green.copy(alpha = 0.35f),
                        disabledContentColor = White.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Verify & Continue", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }

                Spacer(Modifier.height(10.dp))

                OtpResendCountdownRow(
                    otpSent = state.otpSent,
                    onResend = onSendOtp
                )
            }

            else -> {
                CountryPhoneInputRow(
                    selectedCountry = selectedCountry,
                    localDigits = localPhoneDigits,
                    onOpenCountryPicker = onOpenCountryPicker,
                    onLocalDigitsChange = onLocalPhoneChange,
                    onSubmit = {
                        if (localPhoneDigits.length >= 6 && !state.isLoading) {
                            onSendOtp()
                        }
                    }
                )

                Spacer(Modifier.height(14.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = localPhoneDigits.length >= 6 && !state.isLoading,
                    onClick = onSendOtp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = White,
                        disabledContainerColor = Green.copy(alpha = 0.35f),
                        disabledContentColor = White.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Send Verification Code", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun CountryPhoneInputRow(
    selectedCountry: CountryPhoneCode,
    localDigits: String,
    onOpenCountryPicker: () -> Unit,
    onLocalDigitsChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0E121B))
            .border(
                width = 1.2.dp,
                color = if (isFocused) Green else White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onOpenCountryPicker)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedCountry.flag, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${selectedCountry.dialCode} ▾",
                style = UI.typo.nB2.style(color = White, fontWeight = FontWeight.ExtraBold)
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(28.dp)
                .background(White.copy(alpha = 0.16f))
        )

        Spacer(Modifier.width(12.dp))

        BasicTextField(
            value = localDigits,
            onValueChange = onLocalDigitsChange,
            singleLine = true,
            cursorBrush = SolidColor(Green),
            textStyle = UI.typo.nB1.style(
                color = White,
                fontWeight = FontWeight.SemiBold
            ).copy(fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (localDigits.isEmpty()) {
                        Text(
                            text = selectedCountry.placeholder,
                            style = UI.typo.nB1.style(
                                color = White.copy(alpha = 0.35f),
                                fontWeight = FontWeight.Medium
                            ).copy(fontSize = 16.sp)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun OtpPinInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = otp,
        onValueChange = { updated ->
            val digits = updated.filter { it.isDigit() }.take(6)
            onOtpChange(digits)
            if (digits.length == 6 && otp.length < 6) {
                onComplete()
            }
        },
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { if (otp.length == 6) onComplete() }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(6) { index ->
                    val char = otp.getOrNull(index)?.toString() ?: ""
                    val isActive = index == otp.length.coerceAtMost(5) && otp.length < 6
                    val isFilled = char.isNotEmpty()
                    val borderColor = when {
                        isActive -> Green
                        isFilled -> Green.copy(alpha = 0.55f)
                        else -> White.copy(alpha = 0.18f)
                    }
                    val fillColor = if (isActive) {
                        Green.copy(alpha = 0.10f)
                    } else {
                        Color(0xFF0E121B)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(fillColor)
                            .border(
                                width = if (isActive) 2.dp else 1.2.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.ifEmpty { if (isActive) "|" else "•" },
                            color = when {
                                isFilled -> White
                                isActive -> Green
                                else -> White.copy(alpha = 0.25f)
                            },
                            fontSize = if (isFilled) 20.sp else 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun OtpResendCountdownRow(
    otpSent: Boolean,
    onResend: () -> Unit
) {
    var resendTrigger by remember { mutableIntStateOf(0) }
    var secondsLeft by remember(otpSent, resendTrigger) { mutableIntStateOf(30) }

    LaunchedEffect(otpSent, resendTrigger) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        if (secondsLeft > 0) {
            Text(
                text = "Resend code in ${secondsLeft}s",
                style = UI.typo.c.style(
                    color = White.copy(alpha = 0.58f),
                    fontWeight = FontWeight.Medium
                )
            )
        } else {
            Text(
                modifier = Modifier.clickable {
                    resendTrigger += 1
                    onResend()
                },
                text = "Resend verification code",
                style = UI.typo.c.style(
                    color = Green,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun MembershipTiersSection(
    selectedTier: SubscriptionTierOption,
    launchedFromOnboarding: Boolean,
    onSelectTier: (SubscriptionTierOption) -> Unit,
    onUnlockPaidTier: () -> Unit,
    onContinueWithFreeTier: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = "MEMBERSHIP TIERS",
            style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.ExtraBold)
        )

        Spacer(Modifier.height(12.dp))

        // Free Tier (Selected by Default)
        MembershipPlanCard(
            title = "Free Tier",
            subtitle = "\$0 • FOREVER",
            badgeText = if (selectedTier == SubscriptionTierOption.FREE) "SELECTED" else "CURRENT",
            badgeColor = if (selectedTier == SubscriptionTierOption.FREE) Green else Color(0xFFE6C365),
            selected = selectedTier == SubscriptionTierOption.FREE,
            tagline = "All core personal finance tools on your device.",
            features = listOf(
                "Unlimited Accounts, Transactions & Categories" to true,
                "Budgets, Loans & Planned Payments" to true,
                "Local CSV & ZIP Import / Export" to true,
                "Reports & Cloud Backup locked" to false
            ),
            onClick = { onSelectTier(SubscriptionTierOption.FREE) }
        )

        Spacer(Modifier.height(14.dp))

        // Paid Tier ($100 One-Time)
        MembershipPlanCard(
            title = "Paid Tier",
            subtitle = "\$100 ONE-TIME • LIFETIME ACCESS",
            badgeText = if (selectedTier == SubscriptionTierOption.PAID) "SELECTED" else "LIFETIME",
            badgeColor = if (selectedTier == SubscriptionTierOption.PAID) Green else Color(0xFFE6C365),
            selected = selectedTier == SubscriptionTierOption.PAID,
            tagline = "Your backup will sync daily at the end of day in the cloud.",
            features = listOf(
                "Daily end-of-day automatic cloud backup + instant manual backup" to true,
                "Restore your saved data anytime with your mobile number" to true,
                "Full Reports, Balance Sheet & Net Worth analytics" to true,
                "Printable PDF financial statements — pay \$100 once, yours forever" to true
            ),
            onClick = { onSelectTier(SubscriptionTierOption.PAID) }
        )

        Spacer(Modifier.height(18.dp))

        if (selectedTier == SubscriptionTierOption.PAID) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = onUnlockPaidTier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Unlock Paid Tier — \$100 One-Time",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "One-time \$100 payment via Google Pay or Card • No recurring fees",
                style = UI.typo.c.style(
                    color = Gray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        } else if (launchedFromOnboarding) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = onContinueWithFreeTier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Continue with Free Tier",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        } else {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = false,
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = UI.colors.medium,
                    disabledContentColor = UI.colors.gray
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Current Plan — Free Tier",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Select Paid Tier above to upgrade for \$100 one-time",
                style = UI.typo.c.style(
                    color = Gray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun MembershipPlanCard(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    selected: Boolean,
    tagline: String,
    features: List<Pair<String, Boolean>>,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Green else White.copy(alpha = 0.16f)
    val bgBrush = if (selected) {
        Brush.linearGradient(colors = listOf(Color(0xFF142423), Color(0xFF1A2434)))
    } else {
        Brush.linearGradient(colors = listOf(Color(0xFF141923), Color(0xFF1E2636)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(bgBrush)
            .border(if (selected) 1.8.dp else 1.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cashpulse_brand_emblem),
                    contentDescription = "CashPulse",
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = UI.typo.h2.style(color = White, fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = subtitle,
                        style = UI.typo.nC.style(
                            color = Green,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(UI.shapes.rFull)
                    .background(badgeColor.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = badgeText,
                    style = UI.typo.nC.style(
                        color = badgeColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = tagline,
            style = UI.typo.b2.style(
                color = White.copy(alpha = 0.92f),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(14.dp))

        features.forEachIndexed { index, (label, included) ->
            FeatureCheckRow(text = label, included = included)
            if (index < features.lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CountryPickerDialog(
    selectedCountry: CountryPhoneCode,
    onSelectCountry: (CountryPhoneCode) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            CountryPhoneCodes
        } else {
            CountryPhoneCodes.filter {
                it.name.lowercase().contains(q) ||
                    it.dialCode.contains(q) ||
                    it.iso.lowercase().contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF181C26),
        titleContentColor = White,
        textContentColor = White,
        title = {
            Text(
                text = "Select Country",
                style = UI.typo.b1.style(color = White, fontWeight = FontWeight.ExtraBold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search country or code...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = Green,
                        unfocusedBorderColor = White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    items(filtered, key = { "${it.iso}_${it.dialCode}" }) { country ->
                        val isSelected = country.iso == selectedCountry.iso
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Green.copy(alpha = 0.14f) else Color.Transparent
                                )
                                .clickable { onSelectCountry(country) }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = country.flag, fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = country.name,
                                style = UI.typo.b2.style(
                                    color = White,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = country.dialCode,
                                style = UI.typo.nB2.style(
                                    color = if (isSelected) Green else White.copy(alpha = 0.72f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Gray)
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    IvyWalletPreview {
        SubscriptionScreenUi(
            state = SubscriptionState(),
            launchedFromOnboarding = true,
            onSendOtp = {},
            onVerifyOtp = { _, _ -> },
            onResetOtpStep = {},
            onUnlockPaidTier = {},
            onSignOutOnline = {},
            onContinueToWalletSetup = {},
            onClearMessage = {}
        )
    }
}
