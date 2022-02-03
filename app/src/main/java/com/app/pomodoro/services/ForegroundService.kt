package com.app.pomodoro.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.app.pomodoro.MainActivity
import com.app.pomodoro.R
import com.app.pomodoro.extensions.Constants.COMMAND_ID
import com.app.pomodoro.extensions.Constants.COMMAND_START
import com.app.pomodoro.extensions.Constants.COMMAND_STOP
import com.app.pomodoro.extensions.Constants.INTERVAL
import com.app.pomodoro.extensions.Constants.MAX_TIME_MS
import com.app.pomodoro.extensions.Constants.LEFT_TIME_MS
import com.app.pomodoro.extensions.displayTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ForegroundService"

class ForegroundService : Service() {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null
    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro Timer")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    private fun processCommand(intent: Intent?) {
        when (intent?.getStringExtra(COMMAND_ID) ?: return) {
            COMMAND_START -> {
                val leftMs = intent.extras?.getLong(LEFT_TIME_MS) ?: return
                commandStart(leftMs)
            }
            COMMAND_STOP -> commandStop()
        }
    }

    private fun commandStart(leftMs: Long) {
        if (isServiceStarted) return
        try {
            moveToStartedState()
            startForegroundAnsShowNotification()
            continueTimer(leftMs)
        } finally {
            isServiceStarted = true
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAnsShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, importance)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun continueTimer(leftMs: Long) {
        var timeLeft = leftMs
        job = CoroutineScope(Dispatchers.Default).launch {
            while (timeLeft > 0L) {
                timeLeft -= INTERVAL
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(timeLeft.displayTime())
                )
                delay(INTERVAL)
            }
        }
        job?.invokeOnCompletion {
            notificationManager?.notify(NOTIFICATION_ID, getNotification("Time is over!"))
        }
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()

    private fun commandStop() {
        if (!isServiceStarted) return
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private companion object {
        private const val CHANNEL_ID = "CHANNEL_ID"
        private const val NOTIFICATION_ID = 100
    }
}