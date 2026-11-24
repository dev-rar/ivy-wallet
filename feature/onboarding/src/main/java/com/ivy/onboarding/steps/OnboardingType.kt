package com.ivy.onboarding.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ivy.data.cloud.CloudBackupMetadata
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.IvyWalletPreview
import com.ivy.navigation.navigation
import com.ivy.onboarding.components.OnboardingProgressSlider
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.GradientIvy
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Orange
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.CloseButton
import com.ivy.wallet.ui.theme.components.IvyOutlinedButtonFillMaxWidth
import com.ivy.wallet.ui.theme.components.OnboardingButton

@Composable
fun OnboardingType(
    detectedCloudBackup: CloudBackupMetadata? = null,
    isRestoringCloudBackup: Boolean = false,
    cloudStatusMessage: String? = null,
    onRestoreCloudBackup: () -> Unit = {},
    onStartImport: () -> Unit,
    onStartFresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(Modifier.height(16.dp))

        val nav = navigation()
        CloseButton(
            modifier = Modifier.padding(start = 20.dp)
        ) {
            nav.onBackPressed()
        }

        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.import_csv_file),
            style = UI.typo.h2.style(
                fontWeight = FontWeight.Black
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.from_ivy_or_another_app),
            style = UI.typo.nB2.style(
                fontWeight = FontWeight.Bold,
                color = Gray
            )
        )

        Spacer(Modifier.weight(1f))

        Image(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            painter = painterResource(id = R.drawable.onboarding_illustration_import),
            contentDescription = "import illustration"
        )

        OnboardingProgressSlider(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            selectedStep = 0,
            stepsCount = 4,
            selectedColor = Orange
        )

        Spacer(Modifier.weight(1f))

        if (detectedCloudBackup != null) {
            CloudBackupRestoreCard(
                backup = detectedCloudBackup,
                isRestoring = isRestoringCloudBackup,
                onRestore = onRestoreCloudBackup
            )
            Spacer(Modifier.height(12.dp))
        }

        if (!cloudStatusMessage.isNullOrBlank()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = cloudStatusMessage,
                style = UI.typo.c.style(
                    color = Green,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(10.dp))
        }

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.importing_another_time_warning),
            style = UI.typo.b2.style(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(16.dp))

        IvyOutlinedButtonFillMaxWidth(
            modifier = Modifier
                .padding(16.dp),
            text = stringResource(R.string.import_backup_file),
            iconStart = R.drawable.ic_export_csv,
            iconTint = Green,
            textColor = Green
        ) {
            onStartImport()
        }

        Spacer(Modifier.weight(1f))

        OnboardingButton(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.start_fresh),
            textColor = White,
            backgroundGradient = GradientIvy,
            hasNext = true,
            enabled = !isRestoringCloudBackup
        ) {
            onStartFresh()
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CloudBackupRestoreCard(
    backup: CloudBackupMetadata,
    isRestoring: Boolean,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(colors = listOf(Color(0xFF141923), Color(0xFF1E2636)))
            )
            .border(1.dp, Green.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.cashpulse_brand_emblem),
                contentDescription = "CashPulse",
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Cloud Backup Available",
                    style = UI.typo.b1.style(color = White, fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Saved on ${backup.lastSyncFormatted}",
                    style = UI.typo.c.style(color = Green, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Restore your saved accounts, categories, and transactions, or start fresh below.",
            style = UI.typo.c.style(
                color = White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(Modifier.height(12.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isRestoring,
            onClick = onRestore,
            colors = ButtonDefaults.buttonColors(
                containerColor = Green,
                contentColor = White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isRestoring) "Restoring..." else "Restore Cloud Backup",
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    IvyWalletPreview {
        OnboardingType(
            onStartImport = {}
        ) {
        }
    }
}
