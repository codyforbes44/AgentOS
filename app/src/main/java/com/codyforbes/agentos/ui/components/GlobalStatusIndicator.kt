package com.codyforbes.agentos.ui.components

import com.codyforbes.agentos.R

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.ui.formatUsd
import com.codyforbes.agentos.ui.theme.*

/**
 * Top GlobalStatusIndicator component that displays active task counts and total daily token burn rates.
 * Designed for use in the Scaffold header / telemetry status bar.
 */
@Composable
fun GlobalStatusIndicator(
    activeTasks: Int,
    dailyBurnUSD: Double,
    isKillSwitchEngaged: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundDark)
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("global_status_indicator"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active Tasks Count
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (activeTasks > 0) StatusExecuting else StatusOnline)
            )
            Text(
                text = stringResource(R.string.status_active_tasks),
                color = TextMuted,
                fontSize = 11.sp
            )
            Text(
                text = "$activeTasks",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )
        }

        VerticalDivider(
            color = SurfaceBorderDark,
            modifier = Modifier
                .height(14.dp)
                .width(1.dp)
        )

        // Total Daily Token Burn Rate
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = stringResource(R.string.cd_burn_rate),
                tint = CyanAccent,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = stringResource(R.string.status_burn_label),
                color = TextMuted,
                fontSize = 11.sp
            )
            Text(
                text = stringResource(R.string.status_burn_value, formatUsd(dailyBurnUSD)),
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )
        }

        VerticalDivider(
            color = SurfaceBorderDark,
            modifier = Modifier
                .height(14.dp)
                .width(1.dp)
        )

        // System Network Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isKillSwitchEngaged) StatusError else StatusOnline)
            )
            Text(
                text = if (isKillSwitchEngaged) stringResource(R.string.status_halted) else stringResource(R.string.status_system_online),
                color = if (isKillSwitchEngaged) StatusError else StatusOnline,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
