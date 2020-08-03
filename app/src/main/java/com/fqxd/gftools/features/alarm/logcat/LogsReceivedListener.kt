package com.fqxd.gftools.features.alarm.logcat

import androidx.annotation.MainThread
import com.fqxd.gftools.features.alarm.logcat.Log

interface LogsReceivedListener {

    @MainThread
    fun onReceivedLogs(logs: List<Log>)
}