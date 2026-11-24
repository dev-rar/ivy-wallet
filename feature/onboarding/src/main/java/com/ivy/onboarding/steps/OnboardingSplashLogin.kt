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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.IvyWalletPreview
import com.ivy.navigation.SubscriptionScreen
import com.ivy.navigation.navigation
import com.ivy.onboarding.OnboardingState
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.White

private enum class LegalDocType {
    TERMS,
    PRIVACY
}

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
    val nav = navigation()
    var activeLegalDoc by remember { mutableStateOf<LegalDocType?>(null) }

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // Brand Identity Header: Logo + App Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.cashpulse_brand_emblem),
                contentDescription = "CashPulse Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(52.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = White, fontWeight = FontWeight.ExtraBold)) {
                        append("Cash")
                    }
                    withStyle(SpanStyle(color = Green, fontWeight = FontWeight.ExtraBold)) {
                        append("Pulse")
                    }
                },
                fontSize = 26.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        // CashPulse Hero Feature Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1024f / 500f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Green.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.cashpulse_hero_banner),
                contentDescription = "CashPulse Overview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = "CREATE AN ACCOUNT WITH",
            style = UI.typo.c.style(
                color = Green,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(12.dp))

        // Option 1: Offline Account (Always visible, instant local setup)
        AccountOptionCard(
            title = "Offline Account",
            badgeText = "FREE",
            badgeColor = Green,
            subtitle = "Keep your finances private on this device. Start immediately with no sign-up required.",
            highlighted = true,
            onClick = onSkip
        )

        Spacer(Modifier.height(12.dp))

        // Option 2: Online Account (Opens shared Subscription & Mobile Verification screen)
        AccountOptionCard(
            title = "Online Account",
            badgeText = "MOBILE REQUIRED",
            badgeColor = Color(0xFFE6C365),
            subtitle = "Unlock Reports and daily cloud backup (\$100 one-time). Requires mobile verification.",
            highlighted = false,
            onClick = { nav.navigateTo(SubscriptionScreen(launchedFromOnboarding = true)) }
        )

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(18.dp))

        PrivacyPolicyAndTC(
            onOpenTerms = { activeLegalDoc = LegalDocType.TERMS },
            onOpenPrivacy = { activeLegalDoc = LegalDocType.PRIVACY }
        )
    }

    activeLegalDoc?.let { doc ->
        CashPulseLegalDialog(
            docType = doc,
            onDismiss = { activeLegalDoc = null }
        )
    }
}

@Composable
private fun AccountOptionCard(
    title: String,
    badgeText: String,
    badgeColor: Color,
    subtitle: String,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (highlighted) Green else White.copy(alpha = 0.16f)
    val bgBrush = if (highlighted) {
        Brush.linearGradient(colors = listOf(Color(0xFF172627), Color(0xFF141C24)))
    } else {
        Brush.linearGradient(colors = listOf(Color(0xFF1B202C), Color(0xFF141821)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgBrush)
            .border(if (highlighted) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = UI.typo.b1.style(
                    color = White,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(badgeColor.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
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
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = UI.typo.c.style(
                color = White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun CashPulseLegalDialog(
    docType: LegalDocType,
    onDismiss: () -> Unit
) {
    val title = when (docType) {
        LegalDocType.TERMS -> "CashPulse Terms & Conditions"
        LegalDocType.PRIVACY -> "CashPulse Privacy Policy"
    }
    val body = when (docType) {
        LegalDocType.TERMS ->
            "CashPulse is a personal cash-flow, budgeting, and accounting application. " +
                "Offline Account data is stored locally on your device, and you are responsible for maintaining backups. " +
                "Online Account features and secure cloud backup require mobile number verification and the CashPulse Paid tier."
        LegalDocType.PRIVACY ->
            "CashPulse works offline-first by default and keeps your financial records private on your device. " +
                "If you enable an Online Account with mobile number verification, your phone number and encrypted backup are used solely for authentication and syncing your personal data."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = UI.typo.b1.style(fontWeight = FontWeight.ExtraBold))
        },
        text = {
            Text(text = body, style = UI.typo.c.style(fontWeight = FontWeight.Medium))
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PrivacyPolicyAndTC(
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
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
            addStringAnnotation(tag = "LEGAL", annotation = "TERMS", start = tcStart, end = tcEnd)
            addStyle(
                style = SpanStyle(color = Green, textDecoration = TextDecoration.Underline),
                start = tcStart,
                end = tcEnd
            )
        }
        if (privacyEnd > privacyStart) {
            addStringAnnotation(
                tag = "LEGAL",
                annotation = "PRIVACY",
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
        onClick = { offset ->
            annotatedString
                .getStringAnnotations("LEGAL", offset, offset)
                .firstOrNull()
                ?.let { annotation ->
                    if (annotation.item == "TERMS") onOpenTerms() else onOpenPrivacy()
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
