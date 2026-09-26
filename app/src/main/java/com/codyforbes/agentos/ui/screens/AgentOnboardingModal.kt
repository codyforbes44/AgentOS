package com.codyforbes.agentos.ui.screens

import com.codyforbes.agentos.R

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.ui.theme.*
import com.codyforbes.agentos.viewmodel.AgentOSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentOnboardingModal(
    viewModel: AgentOSViewModel,
    onDismiss: () -> Unit
) {
    val name by viewModel.obName.collectAsStateWithLifecycle()
    val provider by viewModel.obProvider.collectAsStateWithLifecycle()
    val endpoint by viewModel.obEndpoint.collectAsStateWithLifecycle()
    val apiKey by viewModel.obApiKey.collectAsStateWithLifecycle()
    val maxCostCap by viewModel.obMaxCostCap.collectAsStateWithLifecycle()
    val timeout by viewModel.obTimeout.collectAsStateWithLifecycle()
    val hitlEnabled by viewModel.obHitlEnabled.collectAsStateWithLifecycle()
    val isPinging by viewModel.isPingTesting.collectAsStateWithLifecycle()
    val pingBadge by viewModel.pingSuccessBadge.collectAsStateWithLifecycle()

    var currentStep by remember { mutableStateOf(1) }

    val providers = listOf("OpenAI", "Anthropic", "Google Gemini", "LangGraph", "Custom REST")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.onboarding_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = stringResource(R.string.onboarding_step_count, currentStep),
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Progress Bar
            LinearProgressIndicator(
                progress = { currentStep / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = StatusExecuting,
                trackColor = SurfaceVariantDark
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (currentStep) {
                1 -> {
                    // Step 1: Provider selection
                    Text(text = stringResource(R.string.onboarding_step_provider), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    providers.forEach { p ->
                        val isSel = provider == p
                        Surface(
                            color = if (isSel) SurfaceVariantDark else BackgroundDark,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyanAccent else SurfaceBorderDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable {
                                    val endpoint = when (p) {
                                        "OpenAI" -> "https://api.openai.com/v1/chat/completions"
                                        "Anthropic" -> "https://api.anthropic.com/v1/messages"
                                        "Google Gemini" -> "https://generativelanguage.googleapis.com/v1beta"
                                        "LangGraph" -> "https://agentos.internal/langgraph/v1"
                                        else -> "https://custom.endpoint.internal/v1/agent"
                                    }
                                    viewModel.onObProviderChange(p, endpoint)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = p, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                RadioButton(selected = isSel, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = CyanAccent))
                            }
                        }
                    }
                }

                2 -> {
                    // Step 2: Endpoint & API Key
                    Text(text = stringResource(R.string.onboarding_step_credentials), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.onObNameChange(it) },
                        label = { Text(stringResource(R.string.onboarding_name_label)) },
                        placeholder = { Text(stringResource(R.string.onboarding_name_placeholder)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                        modifier = Modifier.fillMaxWidth().testTag("ob_agent_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = { viewModel.onObEndpointChange(it) },
                        label = { Text(stringResource(R.string.onboarding_endpoint)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                        modifier = Modifier.fillMaxWidth().testTag("ob_endpoint_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { viewModel.onObApiKeyChange(it) },
                        label = { Text(stringResource(R.string.onboarding_api_key)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                        modifier = Modifier.fillMaxWidth().testTag("ob_api_key_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mandatory security microcopy as per spec
                    Surface(
                        color = BackgroundDark,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = stringResource(R.string.cd_secure_key), tint = StatusOnline, modifier = Modifier.size(16.dp))
                            Text(
                                text = stringResource(R.string.settings_key_note),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                3 -> {
                    // Step 3: Capability Auto-Discovery
                    Text(text = stringResource(R.string.onboarding_step_ping), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.onboarding_ping_body),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.triggerPingTest() },
                        enabled = !isPinging,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("ping_test_button")
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnStatusFill, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.onboarding_ping_running), color = OnStatusFill, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, tint = OnStatusFill)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.onboarding_ping_run), color = OnStatusFill, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (pingBadge != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = StatusOnline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusOnline)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = stringResource(R.string.cd_discovery), tint = StatusOnline)
                                Text(text = pingBadge!!, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                        }
                    }
                }

                4 -> {
                    // Step 4: Hard Caps & Governance Setup
                    Text(text = stringResource(R.string.onboarding_step_caps), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = maxCostCap,
                        onValueChange = { viewModel.onObMaxCostCapChange(it) },
                        label = { Text(stringResource(R.string.onboarding_cost_label)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = timeout,
                        onValueChange = { viewModel.onObTimeoutChange(it) },
                        label = { Text(stringResource(R.string.onboarding_timeout_label)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = stringResource(R.string.onboarding_hitl), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = stringResource(R.string.onboarding_hitl_hint), color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = hitlEnabled,
                            onCheckedChange = { viewModel.onObHitlEnabledChange(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep -= 1 },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.action_back), color = TextSecondary)
                    }
                }

                Button(
                    onClick = {
                        if (currentStep < 4) {
                            currentStep += 1
                        } else {
                            viewModel.deployNewAgent()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusExecuting),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("ob_next_deploy_button")
                ) {
                    Text(
                        text = if (currentStep == 4) stringResource(R.string.action_deploy) else stringResource(R.string.action_continue),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
