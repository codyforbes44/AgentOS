package com.codyforbes.agentos.ui

import java.util.Locale

fun formatUsd(amount: Double): String = String.format(Locale.getDefault(), "%.2f", amount)
