package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

val bottomNavItems = listOf(
    NavItem("Agents", Icons.Filled.SmartToy, Icons.Outlined.SmartToy, "nav_agents"),
    NavItem("Tasks", Icons.Filled.Task, Icons.Outlined.Task, "nav_tasks"),
    NavItem("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics, "nav_analytics"),
    NavItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
)

@Composable
fun AgentOSBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    hitlBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier
    ) {
        bottomNavItems.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (index == 1 && hitlBadgeCount > 0) {
                                Badge(containerColor = StatusApprovalRequired) {
                                    Text(text = "$hitlBadgeCount", color = BackgroundDark)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
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
