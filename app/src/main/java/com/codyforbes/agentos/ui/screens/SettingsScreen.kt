package com.codyforbes.agentos.ui.screens

import com.codyforbes.agentos.R

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val settings by viewModel.settings.collectAsStateWithLifecycle()
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
            text = stringResource(R.string.settings_title),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = stringResource(R.string.settings_subtitle),
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
                        Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = stringResource(R.string.cd_kill_switch), tint = TextPrimary)
                    }

                    Column {
                        Text(
                            text = if (currSettings.emergencyKillSwitchEngaged) {
                                stringResource(R.string.settings_kill_engaged)
                            } else {
                                stringResource(R.string.settings_kill_title)
                            },
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(R.string.settings_kill_subtitle),
                            color = if (currSettings.emergencyKillSwitchEngaged) TextPrimary else StatusError,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exact microcopy as mandated by specification
                Text(
                    text = stringResource(R.string.settings_kill_body),
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
                            .heightIn(min = 48.dp)
                            .testTag("reset_kill_switch_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = OnStatusFill)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.settings_kill_reset), color = OnStatusFill, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { showKillModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("trigger_kill_switch_button")
                    ) {
                        Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, tint = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.settings_kill_engage), color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security & RBAC Access Control Section
        Text(text = stringResource(R.string.settings_rbac_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                    Icon(imageVector = Icons.Default.Key, contentDescription = stringResource(R.string.cd_enclave_key), tint = StatusOnline, modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(R.string.settings_key_note),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SurfaceBorderDark)
                Spacer(modifier = Modifier.height(14.dp))

                Text(text = stringResource(R.string.settings_role_label), color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable { viewModel.updateRole(role) }
                        ) {
                            Text(
                                text = role,
                                color = if (isSel) OnStatusFill else TextPrimary,
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
        Text(text = stringResource(R.string.settings_thresholds_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                    Text(text = stringResource(R.string.settings_daily_limit), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = stringResource(R.string.settings_daily_value, dailyCapSlider.toInt()), color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                        Text(text = stringResource(R.string.settings_circuit_breaker), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(text = stringResource(R.string.settings_circuit_breaker_hint), color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = currSettings.circuitBreakerEnabled,
                        onCheckedChange = { viewModel.updateCircuitBreaker(it) },
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
                        Icon(imageVector = Icons.Default.Warning, contentDescription = stringResource(R.string.cd_warning), tint = StatusError)
                        Text(stringResource(R.string.settings_kill_confirm_title), color = StatusError, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        text = stringResource(R.string.settings_kill_body),
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.triggerEmergencyKillSwitch() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                    ) {
                        Text(stringResource(R.string.settings_kill_confirm), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showKillModal = false }) {
                        Text(stringResource(R.string.action_cancel), color = TextSecondary)
                    }
                }
            )
        }
    }
}
