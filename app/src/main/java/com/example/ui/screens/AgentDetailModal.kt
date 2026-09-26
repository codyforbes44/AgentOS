package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgentEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDetailModal(
    agent: AgentEntity,
    onDelegateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = BackgroundDark.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = agent.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "${agent.provider} • ID: #${agent.id}",
                        color = CyanAccent,
                        fontSize = 12.sp
                    )
                }
                StatusBadge(status = agent.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Configuration Parameters Box
            Surface(
                color = BackgroundDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "ENDPOINT & CREDENTIALS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = agent.endpointUrl, color = TextPrimary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "KEY VAULT REF", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = agent.maskedApiKey, color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = SurfaceBorderDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "DAILY COST CAP", color = TextMuted, fontSize = 10.sp)
                            Text(text = "$${String.format("%.2f", agent.maxCostCapUSD)}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text(text = "TIMEOUT", color = TextMuted, fontSize = 10.sp)
                            Text(text = "${agent.timeoutSeconds}s", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text(text = "HITL GATE", color = TextMuted, fontSize = 10.sp)
                            Text(text = if (agent.hitlEnabled) "ENABLED" else "DISABLED", color = if (agent.hitlEnabled) StatusApprovalRequired else StatusOnline, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Discovered Capabilities Cards
            Text(text = "Discovered Capabilities", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                agent.capabilities.split(",").forEach { cap ->
                    Surface(
                        color = SurfaceVariantDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Extension, contentDescription = cap, tint = CyanAccent, modifier = Modifier.size(14.dp))
                            Text(text = cap.trim(), color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("delete_agent_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Agent", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onDismiss()
                        onDelegateClick()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusExecuting),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("delegate_from_detail_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Delegate Task", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delegate Task", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
