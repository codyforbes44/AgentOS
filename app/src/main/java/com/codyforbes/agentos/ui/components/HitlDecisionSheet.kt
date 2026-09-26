package com.codyforbes.agentos.ui.components

import com.codyforbes.agentos.R

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.data.TaskExecutionEntity
import com.codyforbes.agentos.ui.formatUsd
import com.codyforbes.agentos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HitlDecisionSheet(
    task: TaskExecutionEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SurfaceBorderDark)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // Amber Header Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StatusApprovalRequired.copy(alpha = 0.2f))
                        .border(1.dp, StatusApprovalRequired, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = stringResource(R.string.cd_hitl_gate),
                        tint = StatusApprovalRequired,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.hitl_title),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = stringResource(R.string.hitl_subtitle),
                        color = StatusApprovalRequired,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Details Box
            Surface(
                color = BackgroundDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.hitl_requested_action),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.hitlReason.ifBlank { stringResource(R.string.hitl_default_reason) },
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SurfaceBorderDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = stringResource(R.string.hitl_target_agent), color = TextMuted, fontSize = 10.sp)
                            Text(text = task.agentName, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text(text = stringResource(R.string.hitl_task_id), color = TextMuted, fontSize = 10.sp)
                            Text(text = "#${task.id}", color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                        Column {
                            Text(text = stringResource(R.string.hitl_accumulated_cost), color = TextMuted, fontSize = 10.sp)
                            Text(text = "$${formatUsd(task.actualCostUSD)}", color = StatusOnline, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Context / Security Callout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantDark)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = stringResource(R.string.cd_audit),
                    tint = StatusOnline,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.hitl_audit_note),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Approve vs Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reject_hitl_action")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.hitl_reject), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onApprove,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOnline),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("approve_hitl_action")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = OnStatusFill,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.hitl_approve), color = OnStatusFill, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
