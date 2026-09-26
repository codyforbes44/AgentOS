package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlobalHeader(
    activeTasks: Int,
    hitlCount: Int,
    dailyBurnUSD: Double,
    isKillSwitchEngaged: Boolean,
    onKillSwitchClick: () -> Unit,
    onHitlAlertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDark,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title with Shield / Node icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StatusExecuting.copy(alpha = 0.2f))
                            .border(1.dp, StatusExecuting.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "AgentOS Logo",
                            tint = StatusExecuting,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AgentOS",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.8).sp
                        )
                        Text(
                            text = "v1.0.4-STABLE / SYSTEM_ACTIVE",
                            color = TextSecondary,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Action buttons: HITL Alert & Emergency Kill Switch
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hitlCount > 0) {
                        Surface(
                            color = StatusApprovalRequired.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusApprovalRequired),
                            modifier = Modifier
                                .testTag("hitl_alert_badge")
                                .clickable { onHitlAlertClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "HITL Required",
                                    tint = StatusApprovalRequired,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$hitlCount GATE",
                                    color = StatusApprovalRequired,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Kill switch button
                    IconButton(
                        onClick = onKillSwitchClick,
                        modifier = Modifier
                            .testTag("kill_switch_button")
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isKillSwitchEngaged) StatusError else SurfaceVariantDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Emergency Kill Switch",
                            tint = if (isKillSwitchEngaged) TextPrimary else StatusError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Global Telemetry Pill Bar
            GlobalStatusIndicator(
                activeTasks = activeTasks,
                dailyBurnUSD = dailyBurnUSD,
                isKillSwitchEngaged = isKillSwitchEngaged
            )
        }
    }
}
