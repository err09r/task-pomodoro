package com.app.pomodoro.models

data class Stopwatch(
    val id: Int,
    var leftMs: Long,
    var period: Long,
    var isStarted: Boolean
)