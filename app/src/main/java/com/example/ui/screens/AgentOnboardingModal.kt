package com.example.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AgentOSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentOnboardingModal(
    viewModel: AgentOSViewModel,
    onDismiss: () -> Unit
) {
    val name by viewModel.obName.collectAsState()
    val provider by viewModel.obProvider.collectAsState()
    val endpoint by viewModel.obEndpoint.collectAsState()
    val apiKey by viewModel.obApiKey.collectAsState()
    val maxCostCap by viewModel.obMaxCostCap.collectAsState()
    val timeout by viewModel.obTimeout.collectAsState()
    val hitlEnabled by viewModel.obHitlEnabled.collectAsState()
    val isPinging by viewModel.isPingTesting.collectAsState()
    val pingBadge by viewModel.pingSuccessBadge.collectAsState()

    var currentStep by remember { mutableStateOf(1) }

    val providers = listOf("OpenAI", "Anthropic", "Google Gemini", "LangGraph", "Custom REST")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = BackgroundDark.copy(alpha = 0.8f)
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
                    text = "Agent Onboarding Wizard",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Step $currentStep of 4",
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
                    Text(text = "1. Select Provider Integration", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                                .clickable {
                                    viewModel.obProvider.value = p
                                    viewModel.obEndpoint.value = when (p) {
                                        "OpenAI" -> "https://api.openai.com/v1/chat/completions"
                                        "Anthropic" -> "https://api.anthropic.com/v1/messages"
                                        "Google Gemini" -> "https://generativelanguage.googleapis.com/v1beta"
                                        "LangGraph" -> "https://agentos.internal/langgraph/v1"
                                        else -> "https://custom.endpoint.internal/v1/agent"
                                    }
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
                    Text(text = "2. Endpoint & Credentials", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.obName.value = it },
                        label = { Text("Agent Label / Name") },
                        placeholder = { Text("e.g. FinanceBot-v2") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                        modifier = Modifier.fillMaxWidth().testTag("ob_agent_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = { viewModel.obEndpoint.value = it },
                        label = { Text("Endpoint URL") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                        modifier = Modifier.fillMaxWidth().testTag("ob_endpoint_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { viewModel.obApiKey.value = it },
                        label = { Text("API Key") },
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
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure Key", tint = StatusOnline, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Key encrypted locally via Secure Enclave. Never sent to AgentOS servers.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                3 -> {
                    // Step 3: Capability Auto-Discovery
                    Text(text = "3. Capability Discovery & Ping Test", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AgentOS will execute an automated ping test to discover endpoint tools, model limits, and response latency.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.triggerPingTest() },
                        enabled = !isPinging,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("ping_test_button")
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundDark, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing Endpoint Connection...", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = "Ping Test", tint = BackgroundDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Auto-Discovery Ping Test", color = BackgroundDark, fontWeight = FontWeight.Bold)
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
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Discovery Success", tint = StatusOnline)
                                Text(text = pingBadge!!, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                        }
                    }
                }

                4 -> {
                    // Step 4: Hard Caps & Governance Setup
                    Text(text = "4. Hard Caps & Safety Governance", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = maxCostCap,
                        onValueChange = { viewModel.obMaxCostCap.value = it },
                        label = { Text("Daily Hard Cost Cap ($ USD)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = timeout,
                        onValueChange = { viewModel.obTimeout.value = it },
                        label = { Text("Execution Timeout (seconds)") },
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
                            Text(text = "Require Human-in-the-Loop (HITL)", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = "Gates high-risk actions before execution", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = hitlEnabled,
                            onCheckedChange = { viewModel.obHitlEnabled.value = it },
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
                        Text("Back", color = TextSecondary)
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
                        text = if (currentStep == 4) "Deploy Agent" else "Continue",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
