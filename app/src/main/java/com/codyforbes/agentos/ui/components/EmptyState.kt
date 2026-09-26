package com.codyforbes.agentos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.R
import com.codyforbes.agentos.ui.theme.*

/**
 * Reusable EmptyState component displaying required microcopy and a primary action button.
 */
@Composable
fun EmptyState(
    title: String = stringResource(R.string.empty_agents_title),
    description: String = stringResource(R.string.empty_agents_description),
    actionLabel: String = stringResource(R.string.empty_agents_action),
    icon: ImageVector = Icons.Default.SmartToy,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionTestTag: String = "empty_connect_agent_cta"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("empty_state_container"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag(actionTestTag)
                ) {
                    Text(
                        text = actionLabel,
                        color = OnStatusFill,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Empty state")
@Composable
private fun EmptyStatePreview() {
    AgentOSTheme {
        EmptyState(onActionClick = {})
    }
}
