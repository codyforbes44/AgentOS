package com.codyforbes.agentos.ui.screens

import com.codyforbes.agentos.R

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.codyforbes.agentos.ui.formatUsd
import com.codyforbes.agentos.ui.theme.*
import com.codyforbes.agentos.viewmodel.AgentOSViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AgentOSViewModel,
    modifier: Modifier = Modifier
) {
    val timeRange by viewModel.analyticsTimeRange.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val totalBurnUSD by viewModel.totalDailyBurnUSD.collectAsStateWithLifecycle()

    val clipboardManager = LocalClipboardManager.current
    var showExportToast by remember { mutableStateOf<String?>(null) }
    val copiedCsv = stringResource(R.string.analytics_copied_csv)
    val copiedJson = stringResource(R.string.analytics_copied_json)
    val copiedPdf = stringResource(R.string.analytics_copied_pdf)

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
                    text = stringResource(R.string.analytics_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = (-1.0).sp
                )
                Text(
                    text = stringResource(R.string.analytics_subtitle),
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
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clickable { viewModel.setAnalyticsTimeRange(range) }
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
                    text = stringResource(R.string.analytics_estimated_daily),
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
                        text = "$${formatUsd(totalBurnUSD)}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 42.sp,
                        letterSpacing = (-1.5).sp
                    )
                    Text(
                        text = stringResource(R.string.analytics_window),
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SurfaceVariantDark)
                Spacer(modifier = Modifier.height(16.dp))

                // Hero Substats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = stringResource(R.string.analytics_input_tokens), color = TextMuted, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(text = stringResource(R.string.analytics_input_value), color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Column {
                        Text(text = stringResource(R.string.analytics_output_tokens), color = TextMuted, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(text = stringResource(R.string.analytics_output_value), color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Column {
                        Text(text = stringResource(R.string.analytics_reasoning), color = TextMuted, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(text = stringResource(R.string.analytics_reasoning_value), color = CyanAccent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
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
                    Text(text = stringResource(R.string.analytics_chart_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = stringResource(R.string.analytics_total_burn_value, formatUsd(totalBurnUSD)), color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = stringResource(R.string.cd_insights), tint = CyanAccent)
                Column {
                    Text(text = stringResource(R.string.analytics_insights_title), color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.analytics_insights_body),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reliability Metrics Grid
        Text(text = stringResource(R.string.analytics_reliability), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                    Text(text = stringResource(R.string.analytics_latency), color = TextMuted, fontSize = 10.sp)
                    Text(text = stringResource(R.string.analytics_latency_value), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = stringResource(R.string.analytics_success), color = TextMuted, fontSize = 10.sp)
                    Text(text = stringResource(R.string.analytics_success_value), color = StatusOnline, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = stringResource(R.string.analytics_error_rate), color = TextMuted, fontSize = 10.sp)
                    Text(text = stringResource(R.string.analytics_error_value), color = StatusOnline, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Export & Reporting Engine Section
        Text(text = stringResource(R.string.analytics_export_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val csv = viewModel.exportTelemetryReport("CSV")
                    clipboardManager.setText(AnnotatedString(csv))
                    showExportToast = copiedCsv
                },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("export_csv_button")
            ) {
                Text(stringResource(R.string.analytics_export_csv), color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val json = viewModel.exportTelemetryReport("JSON")
                    clipboardManager.setText(AnnotatedString(json))
                    showExportToast = copiedJson
                },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("export_json_button")
            ) {
                Text(stringResource(R.string.analytics_export_json), color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    showExportToast = copiedPdf
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("export_pdf_button")
            ) {
                Text(stringResource(R.string.analytics_export_pdf), color = OnStatusFill, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
