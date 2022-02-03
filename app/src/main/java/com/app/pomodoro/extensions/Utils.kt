package com.app.pomodoro.extensions

fun Long.displayTime(): String {
    if (this < 0L) return "00:00:00"

    val h = this / 1000 / 3600
    val m = this / 1000 % 3600 / 60
    val s = this / 1000 % 60
    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
}

private fun displaySlot(count: Long) = if (count > 9L) "$count" else "0$count"