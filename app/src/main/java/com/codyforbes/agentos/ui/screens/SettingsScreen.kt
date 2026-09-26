package com.codyforbes.agentos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.data.SystemSettingsEntity
import com.codyforbes.agentos.ui.theme.*
import com.codyforbes.agentos.viewmodel.AgentOSViewModel

@Composable
fun SettingsScreen(
    viewModel: AgentOSViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val currSettings = settings ?: SystemSettingsEntity()

    var dailyCapSlider by remember(currSettings) { mutableStateOf(currSettings.dailyCostCapUSD.toFloat()) }
    var showKillModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "System Governance & Security",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "Access control, circuit breakers & global kill switch",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // EMERGENCY KILL SWITCH CARD (CRITICAL FEATURE)
        Surface(
            color = if (currSettings.emergencyKillSwitchEngaged) StatusError else StatusError.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusError),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = "Kill Switch", tint = TextPrimary)
                    }

                    Column {
                        Text(
                            text = if (currSettings.emergencyKillSwitchEngaged) "KILL SWITCH ENGAGED" else "Emergency Kill Switch",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Global System Circuit Breaker",
                            color = if (currSettings.emergencyKillSwitchEngaged) TextPrimary else StatusError,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exact microcopy as mandated by specification
                Text(
                    text = "Stop all active execution threads immediately? Unsaved task progress will be lost.",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (currSettings.emergencyKillSwitchEngaged) {
                    Button(
                        onClick = { viewModel.resetKillSwitch() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusOnline),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("reset_kill_switch_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = BackgroundDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Operational State", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { showKillModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("trigger_kill_switch_button")
                    ) {
                        Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = "Kill All", tint = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ENGAGE EMERGENCY KILL SWITCH", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security & RBAC Access Control Section
        Text(text = "Security & Role-Based Access Control (RBAC)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = "Enclave Key", tint = StatusOnline, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Key encrypted locally via Secure Enclave. Never sent to AgentOS servers.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = SurfaceBorderDark)
                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Active RBAC Role Selection", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Viewer", "Operator", "Admin").forEach { role ->
                        val isSel = currSettings.activeRole == role
                        Surface(
                            color = if (isSel) CyanAccent else SurfaceVariantDark,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.updateRole(role) }
                        ) {
                            Text(
                                text = role,
                                color = if (isSel) BackgroundDark else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Global Spending Thresholds & Circuit Breakers
        Text(text = "Global Cost & Safety Thresholds", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Daily Organization Spending Limit", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = "$${dailyCapSlider.toInt()} USD", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Slider(
                    value = dailyCapSlider,
                    onValueChange = { dailyCapSlider = it },
                    onValueChangeFinished = { viewModel.updateDailyCap(dailyCapSlider.toDouble()) },
                    valueRange = 10f..500f,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent, inactiveTrackColor = SurfaceVariantDark),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Automatic Circuit Breaker", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(text = "Auto-pause agents upon spending limit breach", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = currSettings.circuitBreakerEnabled,
                        onCheckedChange = { },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirmation Modal for Emergency Kill Switch
        if (showKillModal) {
            AlertDialog(
                onDismissRequest = { showKillModal = false },
                containerColor = SurfaceDark,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning", tint = StatusError)
                        Text("Confirm Emergency Kill Switch", color = StatusError, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        text = "Stop all active execution threads immediately? Unsaved task progress will be lost.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.triggerEmergencyKillSwitch() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                    ) {
                        Text("YES, HALT ALL EXECUTIONS", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showKillModal = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
