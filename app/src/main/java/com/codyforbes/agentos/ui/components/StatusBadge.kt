package com.codyforbes.agentos.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.ui.theme.*

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon, label) = when (status.uppercase()) {
        "ONLINE_IDLE", "ONLINE" -> Quadruple(
            StatusOnline.copy(alpha = 0.15f),
            StatusOnline,
            Icons.Default.CheckCircle,
            "ONLINE IDLE"
        )
        "EXECUTING", "RUNNING" -> Quadruple(
            StatusExecuting.copy(alpha = 0.15f),
            StatusExecuting,
            Icons.Default.Sync,
            "EXECUTING"
        )
        "AWAITING_APPROVAL", "AWAITING_HITL" -> Quadruple(
            StatusApprovalRequired.copy(alpha = 0.2f),
            StatusApprovalRequired,
            Icons.Default.Warning,
            "HITL GATE"
        )
        "RATE_LIMITED" -> Quadruple(
            StatusRateLimited.copy(alpha = 0.2f),
            StatusRateLimited,
            Icons.Default.HourglassTop,
            "THROTTLED"
        )
        "ERROR", "FAILED" -> Quadruple(
            StatusError.copy(alpha = 0.2f),
            StatusError,
            Icons.Default.Error,
            "ERROR"
        )
        "COMPLETED" -> Quadruple(
            StatusOnline.copy(alpha = 0.2f),
            StatusOnline,
            Icons.Default.CheckCircle,
            "COMPLETED"
        )
        "KILLED" -> Quadruple(
            StatusError.copy(alpha = 0.25f),
            StatusError,
            Icons.Default.Cancel,
            "KILLED"
        )
        else -> Quadruple(
            StatusOffline.copy(alpha = 0.2f),
            StatusOffline,
            Icons.Default.Pause,
            "OFFLINE"
        )
    }

    // Pulse animation for EXECUTING or AWAITING_APPROVAL
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val isPulsing = status == "EXECUTING" || status == "RUNNING" || status == "AWAITING_APPROVAL" || status == "AWAITING_HITL"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .alpha(if (isPulsing) alphaAnim else 1f)
                .clip(CircleShape)
                .background(textColor)
        )
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
