package com.fqxd.gftools.features.alarm.logcat

import androidx.annotation.MainThread

interface LogsReceivedListener {

    @MainThread
    fun onReceivedLogs(logs: List<Log>)
}