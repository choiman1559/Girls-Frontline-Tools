package com.fqxd.gftools.features.alarm.logcat

interface Filter {

    fun apply(log: Log): Boolean
}