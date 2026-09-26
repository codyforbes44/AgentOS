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
import com.example.data.TaskExecutionEntity
import com.example.ui.theme.*

@Composable
fun TaskCard(
    task: TaskExecutionEntity,
    onClick: () -> Unit,
    onHitlClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHitlRequired = task.status == "AWAITING_HITL"
    val statusColor = when (task.status) {
        "AWAITING_HITL" -> StatusApprovalRequired
        "COMPLETED" -> StatusOnline
        "FAILED", "KILLED" -> StatusError
        "EXECUTING", "QUEUED" -> StatusExecuting
        else -> StatusOffline
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isHitlRequired) StatusApprovalRequired else SurfaceBorderDark,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag("task_card_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left status bar accent
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
                // Header Row: Title & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = (-0.3).sp
                        )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = task.agentName,
                            color = CyanAccent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "•",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Surface(
                            color = SurfaceVariantDark,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = task.flowType,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = task.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Objective preview
            Text(
                text = task.objective,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step Progress Bar
            val progress = if (task.totalSteps > 0) task.currentStepIndex.toFloat() / task.totalSteps else 0f
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Step ${task.currentStepIndex} of ${task.totalSteps}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when (task.status) {
                        "AWAITING_HITL" -> StatusApprovalRequired
                        "COMPLETED" -> StatusOnline
                        "FAILED", "KILLED" -> StatusError
                        else -> StatusExecuting
                    },
                    trackColor = SurfaceVariantDark
                )
            }

            // HITL Action Banner if awaiting approval
            if (isHitlRequired) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = StatusApprovalRequired.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusApprovalRequired.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Intervention Required",
                                tint = StatusApprovalRequired,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Human Intervention Required",
                                color = StatusApprovalRequired,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.hitlReason,
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onHitlClick,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusApprovalRequired),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(32.dp)
                                .testTag("review_hitl_button")
                        ) {
                            Text(
                                text = "Review & Approve",
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
}
