package com.zscaler.utils

import kotlin.reflect.jvm.internal.impl.incremental.components.LookupTracker.DO_NOTHING

object PANELTYPE {
    val AUTO_CHOOSE_PANEL = 0
    val SCAN_FINISHED_EMPTY = 1
    val SCAN_FINISHED = 2
    val SCAN_STARTED = 3
    val SCAN_ERROR = 4
    val PRE_SCAN = 5
    val SCAN_PARSING_ERROR = 6
    val INSTALATION_STARTED = 7
    val DO_NOTHING = 8
}

val API_URL = "https://main.dev.api.zscwp.io"
val AUTH_URL = "https://main.dev.api.zscwp.io"

val DEFAULT_TIMEOUT: Long = 80000