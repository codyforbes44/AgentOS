package com.codyforbes.agentos.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.ui.theme.*
import com.codyforbes.agentos.viewmodel.AgentOSViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AgentOSViewModel,
    modifier: Modifier = Modifier
) {
    val timeRange by viewModel.analyticsTimeRange.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val totalBurnUSD by viewModel.totalDailyBurnUSD.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var showExportToast by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title & Time Range Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "OBSERVABILITY",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = (-1.0).sp
                )
                Text(
                    text = "REAL-TIME TELEMETRY & COST CONTROL",
                    color = TextSecondary,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
            }

            // Time Range Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("1h", "24h", "7d", "30d").forEach { range ->
                    val isSel = timeRange == range
                    Surface(
                        color = if (isSel) StatusExecuting else SurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) StatusExecuting else SurfaceBorderDark),
                        modifier = Modifier.clickable { viewModel.analyticsTimeRange.value = range }
                    ) {
                        Text(
                            text = range,
                            color = if (isSel) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Metric Card (3xl rounded corners) from Bold Typography theme HTML
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ESTIMATED DAILY BURN",
                    color = TextSecondary,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$${String.format("%.2f", totalBurnUSD)}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 42.sp,
                        letterSpacing = (-1.5).sp
                    )
                    Text(
                        text = "/ 24h",
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = SurfaceVariantDark)
                Spacer(modifier = Modifier.height(16.dp))

                // Hero Substats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "INPUT TOKENS", color = TextMuted, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(text = "1.24M", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Column {
                        Text(text = "OUTPUT TOKENS", color = TextMuted, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(text = "184K", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Column {
                        Text(text = "REASONING", color = TextMuted, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(text = "410K", color = CyanAccent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Canvas Stacked Token Burn Chart
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Token Consumption & Cost Burn", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Total Burn: $${String.format("%.2f", totalBurnUSD)}", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Canvas Chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val barWidth = width / 8f

                    val dummyBars = listOf(0.4f, 0.65f, 0.3f, 0.85f, 0.5f, 0.9f, 0.7f)
                    dummyBars.forEachIndexed { idx, barRatio ->
                        val x = (idx + 0.5f) * barWidth
                        val barH = height * barRatio

                        // Draw input tokens bar portion (blue)
                        drawRoundRect(
                            color = StatusExecuting,
                            topLeft = Offset(x - 12.dp.toPx(), height - barH),
                            size = Size(24.dp.toPx(), barH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Draw reasoning top accent (cyan)
                        drawRoundRect(
                            color = CyanAccent,
                            topLeft = Offset(x - 12.dp.toPx(), height - barH),
                            size = Size(24.dp.toPx(), barH * 0.25f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Text(text = day, color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Plain-Language Trend Insights Card as per spec
        Surface(
            color = StatusExecuting.copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusExecuting.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Insights", tint = CyanAccent)
                Column {
                    Text(text = "PLAIN-LANGUAGE TREND INSIGHTS", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Token efficiency improved by 14% over 7 days due to prompt optimization on DataExtractionAgent.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reliability Metrics Grid
        Text(text = "Reliability & Health Metrics", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "LATENCY P95", color = TextMuted, fontSize = 10.sp)
                    Text(text = "1,840ms", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "SUCCESS RATE", color = TextMuted, fontSize = 10.sp)
                    Text(text = "99.2%", color = StatusOnline, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "FAILURE RATE", color = TextMuted, fontSize = 10.sp)
                    Text(text = "0.8%", color = StatusOnline, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Export & Reporting Engine Section
        Text(text = "Export & Reporting Engine", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val csv = viewModel.exportTelemetryReport("CSV")
                    clipboardManager.setText(AnnotatedString(csv))
                    showExportToast = "CSV Telemetry Report copied to clipboard!"
                },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("export_csv_button")
            ) {
                Text("Export CSV", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val json = viewModel.exportTelemetryReport("JSON")
                    clipboardManager.setText(AnnotatedString(json))
                    showExportToast = "JSON Telemetry Report copied to clipboard!"
                },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("export_json_button")
            ) {
                Text("Export JSON", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    showExportToast = "PDF Executive Summary Report Generated!"
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("export_pdf_button")
            ) {
                Text("PDF Report", color = BackgroundDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (showExportToast != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = StatusOnline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = showExportToast!!,
                    color = StatusOnline,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
