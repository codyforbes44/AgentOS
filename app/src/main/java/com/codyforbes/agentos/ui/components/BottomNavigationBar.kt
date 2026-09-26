package com.codyforbes.agentos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.R
import com.codyforbes.agentos.ui.theme.*

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
private fun bottomNavItems(): List<NavItem> = listOf(
    NavItem(stringResource(R.string.nav_agents), Icons.Filled.SmartToy, Icons.Outlined.SmartToy, "nav_agents"),
    NavItem(stringResource(R.string.nav_tasks), Icons.Filled.Task, Icons.Outlined.Task, "nav_tasks"),
    NavItem(stringResource(R.string.nav_analytics), Icons.Filled.Analytics, Icons.Outlined.Analytics, "nav_analytics"),
    NavItem(stringResource(R.string.nav_settings), Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings"),
)

@Composable
fun AgentOSBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    hitlBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val items = bottomNavItems()
    val badgeDescription = stringResource(R.string.cd_hitl_badge_count, hitlBadgeCount)
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (index == 1 && hitlBadgeCount > 0) {
                                Badge(containerColor = StatusApprovalRequired) {
                                    Text(
                                        text = "$hitlBadgeCount",
                                        color = OnStatusFill,
                                        modifier = Modifier.clearAndSetSemantics {
                                            contentDescription = badgeDescription
                                        },
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                        letterSpacing = (-0.2).sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StatusExecuting,
                    selectedTextColor = StatusExecuting,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = SurfaceVariantDark
                ),
                modifier = Modifier.testTag(item.tag)
            )
        }
    }
}
