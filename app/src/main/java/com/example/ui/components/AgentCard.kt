package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.*

@Composable
fun AgentCard(
    agent: AgentEntity,
    onClick: () -> Unit,
    onDelegateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (agent.status) {
        "ONLINE_IDLE" -> StatusOnline
        "EXECUTING" -> StatusExecuting
        "AWAITING_APPROVAL" -> StatusApprovalRequired
        "RATE_LIMITED" -> StatusRateLimited
        else -> StatusError
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("agent_card_${agent.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Bold status color left bar accent from theme HTML
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row: Provider Icon + Agent Name + Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val providerIcon = when (agent.provider) {
                                "OpenAI" -> Icons.Default.AutoAwesome
                                "Anthropic" -> Icons.Default.Psychology
                                "LangGraph" -> Icons.Default.AccountTree
                                else -> Icons.Default.Api
                            }
                            Icon(
                                imageVector = providerIcon,
                                contentDescription = agent.provider,
                                tint = statusColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = agent.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "${agent.provider} • ${agent.endpointUrl.take(28)}...",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    StatusBadge(status = agent.status)
                }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Row: Health Score & Token Burn
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "HEALTH SCORE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { agent.healthScore / 100f },
                            modifier = Modifier.size(16.dp),
                            color = if (agent.healthScore > 90) StatusOnline else StatusApprovalRequired,
                            strokeWidth = 3.dp,
                            trackColor = SurfaceVariantDark
                        )
                        Text(
                            text = "${agent.healthScore}%",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Column {
                    Text(text = "DAILY TOKEN BURN", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${agent.dailyTokenBurn / 1000}k tokens",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Column {
                    Text(text = "TOTAL COST", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$${String.format("%.2f", agent.totalCostUSD)}",
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tags + Delegate Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    agent.tags.split(",").take(3).forEach { tag ->
                        Surface(
                            color = SurfaceVariantDark,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = tag.trim(),
                                color = TextSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Delegate button
                OutlinedButton(
                    onClick = onDelegateClick,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusExecuting),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusExecuting),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Delegate",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Delegate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
}
