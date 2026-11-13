package com.ivy.onboarding.steps

import android.app.Activity
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.cloud.CashPulseCloudAccountManager
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.Constants
import com.ivy.legacy.IvyWalletPreview
import com.ivy.onboarding.OnboardingState
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.White

@Suppress("UnusedParameter")
@Composable
fun BoxWithConstraintsScope.OnboardingSplashLogin(
    onboardingState: OnboardingState,
    otpSent: Boolean = false,
    cloudStatusMessage: String? = null,
    isPro: Boolean = false,
    onSendOtp: (Activity?, String) -> Unit = { _, _ -> },
    onVerifyOtp: (String, String) -> Unit = { _, _ -> },
    onUpgradePro: (Activity?, String) -> Unit = { _, _ -> },
    onSkip: () -> Unit = {},
) {
    if (onboardingState == OnboardingState.SPLASH) {
        BrandedSplashView()
        return
    }

    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0E1117), Color(0xFF151A24), Color(0xFF0D1016))
                )
            )
            .systemBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header: Brand Lockup & Step Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.cashpulse_brand_lockup),
                contentDescription = "CashPulse Brand",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(34.dp)
                    .width(150.dp)
            )
            Text(
                text = "Step ${currentStep + 1} / $totalSteps",
                style = UI.typo.c.style(
                    color = Green,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // Main Scrollable Step Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            when (currentStep) {
                0 -> WelcomeStep()
                1 -> FeaturesAccountingStep()
                2 -> PlansPricingStep(
                    isPro = isPro,
                    cloudStatusMessage = cloudStatusMessage,
                    onUpgradePro = onUpgradePro
                )
                else -> MandatoryPhoneOtpStep(
                    otpSent = otpSent,
                    cloudStatusMessage = cloudStatusMessage,
                    onSendOtp = onSendOtp,
                    onVerifyOtp = onVerifyOtp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Step Progress Indicator Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val isSelected = index == currentStep
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (isSelected) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Green else White.copy(alpha = 0.25f))
                        .clickable { currentStep = index }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Bottom Navigation Row (Back / Next or Jump to Phone Verification)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    onClick = { currentStep -= 1 },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Back",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (currentStep < totalSteps - 1) {
                Button(
                    modifier = Modifier
                        .weight(if (currentStep > 0) 1.6f else 1f)
                        .height(50.dp),
                    onClick = { currentStep += 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (currentStep == totalSteps - 2) {
                            "Continue to Phone Sign-In →"
                        } else {
                            "Next →"
                        },
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        if (currentStep < totalSteps - 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { currentStep = totalSteps - 1 }) {
                    Text(
                        text = "Already registered? Sign In with Phone →",
                        style = UI.typo.c.style(
                            color = White.copy(alpha = 0.72f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
            PrivacyPolicyAndTC()
        }
    }
}

@Composable
private fun BrandedSplashView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1117)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.cashpulse_brand_emblem),
                contentDescription = "CashPulse Emblem",
                modifier = Modifier.size(132.dp)
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = "CashPulse",
                style = UI.typo.h1.style(
                    color = White,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Smart Cash Flow & Personal Accounting",
                style = UI.typo.b2.style(
                    color = Green,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF232937), Color(0xFF151922))
                    )
                )
                .border(1.dp, Green.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.cashpulse_brand_emblem),
                    contentDescription = "CashPulse Brand Emblem",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Welcome to CashPulse",
                    style = UI.typo.h2.style(
                        color = White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Smart Cash Flow, Budgeting & Personal Accounting — Built Offline-First with Permanent Phone-Verified Cloud Protection.",
                    style = UI.typo.b2.style(
                        color = White.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        OnboardingHighlightRow(
            title = "100% Offline-First Speed",
            subtitle = "Log income, expenses, transfers, and loans anytime — even with zero internet connection."
        )
        Spacer(Modifier.height(10.dp))
        OnboardingHighlightRow(
            title = "Phone Number Identity (No Google Login Required)",
            subtitle = "Your phone number + SMS OTP is your permanent CashPulse key across devices and reinstalls."
        )
    }
}

@Composable
private fun FeaturesAccountingStep() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What CashPulse Gives You",
            style = UI.typo.h2.style(
                color = White,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "More than an expense tracker — real accounting and automation in your pocket.",
            style = UI.typo.b2.style(
                color = White.copy(alpha = 0.72f),
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(Modifier.height(16.dp))

        OnboardingHighlightRow(
            title = "True Balance Sheet & Net Worth",
            subtitle = "Live Assets vs. Liabilities equation, Lending Receivables, Borrowing Payables, and Debt-to-Asset ratio."
        )
        Spacer(Modifier.height(10.dp))
        OnboardingHighlightRow(
            title = "Annual Cash-Flow & PDF Statements",
            subtitle = "Month-by-month trends, category budget progress, and 1-tap printable PDF financial statements."
        )
        Spacer(Modifier.height(10.dp))
        OnboardingHighlightRow(
            title = "Account Reconciliation & Bank Auto-Tracker",
            subtitle = "Reconcile wallet balances against bank statements and auto-capture bank SMS/app alerts on-device."
        )
        Spacer(Modifier.height(10.dp))
        OnboardingHighlightRow(
            title = "Automatic Firebase Cloud Backup",
            subtitle = "Changes automatically sync to your encrypted Firebase Cloud record so switching phones never loses data."
        )
    }
}

@Composable
private fun PlansPricingStep(
    isPro: Boolean,
    cloudStatusMessage: String?,
    onUpgradePro: (Activity?, String) -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Choose Free or Go Pro",
            style = UI.typo.h2.style(
                color = White,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Start free with core offline tracking, or unlock the full CashPulse Pro suite via Google Pay or Card.",
            style = UI.typo.b2.style(
                color = White.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(Modifier.height(14.dp))

        // Free Tier Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(White.copy(alpha = 0.06f))
                .border(1.dp, White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "FREE TIER — \$0 (Limited Features)",
                style = UI.typo.c.style(color = White.copy(alpha = 0.7f), fontWeight = FontWeight.ExtraBold)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "• Offline Income, Expense & Transfer tracking\n" +
                    "• Accounts, Categories & Loans (Local storage only)\n" +
                    "• Manual local JSON/CSV export",
                style = UI.typo.c.style(color = White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Pro Tier Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1F2A38), Color(0xFF131922))
                    )
                )
                .border(1.5.dp, Green, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = if (isPro) "CASHPULSE PRO — ACTIVE ✓" else "CASHPULSE PRO — FULL ACCESS",
                style = UI.typo.c.style(color = Green, fontWeight = FontWeight.ExtraBold)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "• Full Financial Reports, Balance Sheet & Net Worth\n" +
                    "• Printable PDF Statements & Category Trends\n" +
                    "• Permanent Automatic Firebase Cloud Backup & Restore\n" +
                    "• On-Device Bank SMS & Push Alert Auto-Tracker",
                style = UI.typo.c.style(color = White, fontWeight = FontWeight.SemiBold)
            )

            if (!cloudStatusMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = cloudStatusMessage,
                    style = UI.typo.c.style(color = Green, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onUpgradePro(context as? Activity, CashPulseCloudAccountManager.PLAN_MONTHLY)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Monthly Pro — \$10 / month (Google Pay / Card)", fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(6.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onUpgradePro(context as? Activity, CashPulseCloudAccountManager.PLAN_YEARLY)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = White.copy(alpha = 0.14f),
                    contentColor = White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Yearly Pro — \$100 / year (Save \$20)", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(6.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onUpgradePro(context as? Activity, CashPulseCloudAccountManager.PLAN_LIFETIME)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = White.copy(alpha = 0.08f),
                    contentColor = White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lifetime Pro — \$500 One-Time", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MandatoryPhoneOtpStep(
    otpSent: Boolean,
    cloudStatusMessage: String?,
    onSendOtp: (Activity?, String) -> Unit,
    onVerifyOtp: (String, String) -> Unit
) {
    val context = LocalContext.current
    var phoneInput by remember { mutableStateOf("+") }
    var otpInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1E2430), Color(0xFF141821))
                )
            )
            .border(1.dp, Green.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "REQUIRED ACCOUNT REGISTRATION",
            style = UI.typo.c.style(color = Green, fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Register / Sign In with Phone",
            style = UI.typo.h2.style(color = White, fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Phone verification via SMS OTP is required to use CashPulse. If you already have a Firebase Cloud backup on this number, your accounts, budgets, and transactions will restore automatically.",
            style = UI.typo.c.style(
                color = White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            label = { Text("Mobile Number (+CountryCode...)") },
            placeholder = { Text("+923001234567") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = White,
                unfocusedTextColor = White,
                focusedBorderColor = Green,
                unfocusedBorderColor = White.copy(alpha = 0.35f),
                focusedLabelColor = Green,
                unfocusedLabelColor = White.copy(alpha = 0.65f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (otpSent) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = otpInput,
                onValueChange = { if (it.length <= 6) otpInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("6-Digit SMS OTP Code") },
                placeholder = { Text("123456") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = Green,
                    unfocusedBorderColor = White.copy(alpha = 0.35f),
                    focusedLabelColor = Green,
                    unfocusedLabelColor = White.copy(alpha = 0.65f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!cloudStatusMessage.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = cloudStatusMessage,
                style = UI.typo.c.style(color = Green, fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (!otpSent) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = { onSendOtp(context as? Activity, phoneInput) },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Send SMS OTP Code", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
        } else {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = { onVerifyOtp(phoneInput, otpInput) },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Verify OTP & Continue →", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onSendOtp(context as? Activity, phoneInput) }
            ) {
                Text("Resend OTP Code", color = White.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun OnboardingHighlightRow(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White.copy(alpha = 0.06f))
            .border(1.dp, White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            style = UI.typo.b2.style(color = Green, fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = UI.typo.c.style(color = White.copy(alpha = 0.78f), fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun PrivacyPolicyAndTC() {
    val terms = stringResource(R.string.terms_conditions)
    val privacy = stringResource(R.string.privacy_policy)
    val text = stringResource(R.string.by_signing_in, terms, privacy)

    val tcStart = text.indexOf(terms).coerceAtLeast(0)
    val tcEnd = (tcStart + terms.length).coerceAtMost(text.length)

    val privacyStart = text.indexOf(privacy).coerceAtLeast(0)
    val privacyEnd = (privacyStart + privacy.length).coerceAtMost(text.length)

    val annotatedString = buildAnnotatedString {
        append(text)
        if (tcEnd > tcStart) {
            addStringAnnotation(tag = "URL", annotation = Constants.URL_TC, start = tcStart, end = tcEnd)
            addStyle(
                style = SpanStyle(color = Green, textDecoration = TextDecoration.Underline),
                start = tcStart,
                end = tcEnd
            )
        }
        if (privacyEnd > privacyStart) {
            addStringAnnotation(
                tag = "URL",
                annotation = Constants.URL_PRIVACY_POLICY,
                start = privacyStart,
                end = privacyEnd
            )
            addStyle(
                style = SpanStyle(color = Green, textDecoration = TextDecoration.Underline),
                start = privacyStart,
                end = privacyEnd
            )
        }
    }

    val uriHandler = LocalUriHandler.current
    ClickableText(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        text = annotatedString,
        style = UI.typo.c.style(
            color = White.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        ),
        onClick = {
            annotatedString
                .getStringAnnotations("URL", it, it)
                .forEach { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    IvyWalletPreview {
        OnboardingSplashLogin(
            onboardingState = OnboardingState.LOGIN
        )
    }
}
