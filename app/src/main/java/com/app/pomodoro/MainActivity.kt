package com.app.pomodoro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.pomodoro.extensions.Constants.COMMAND_ID
import com.app.pomodoro.extensions.Constants.COMMAND_START
import com.app.pomodoro.extensions.Constants.COMMAND_STOP
import com.app.pomodoro.extensions.Constants.LEFT_TIME_MS
import com.app.pomodoro.databinding.ActivityMainBinding
import com.app.pomodoro.extensions.Constants.MAX_TIME_MS
import com.app.pomodoro.models.Stopwatch
import com.app.pomodoro.recyclerview.StopwatchAdapter
import com.app.pomodoro.services.ForegroundService

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), StopwatchListener {

    private lateinit var binding: ActivityMainBinding

    private val observer = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            onAppForegrounded()
        }

        override fun onStop(owner: LifecycleOwner) {
            onAppBackgrounded()
        }
    }

    private val stopwatchList = mutableListOf<Stopwatch>()
    private val stopwatchAdapter = StopwatchAdapter(this)
    private var nextId = 0
    private var activeId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(observer)
        initViews()
    }

    override fun start(id: Int) {
        changeStopwatch(id, true)
    }

    override fun stop(id: Int) {
        changeStopwatch(id, false)
    }

    override fun delete(id: Int) {
        stopwatchList.remove(stopwatchList.find { it.id == id })
        stopwatchAdapter.submitList(stopwatchList.toList())
    }

    private fun changeStopwatch(id: Int, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatchList.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, it.leftMs, it.period, isStarted))
            } else {
                newTimers.add(Stopwatch(it.id, it.leftMs, it.period, false))
            }
        }
        activeId = id
        stopwatchAdapter.submitList(newTimers)
        stopwatchList.clear()
        stopwatchList.addAll(newTimers)
    }

    private fun initViews() {
        with(binding) {
            numberPicker.apply {
                minValue = MIN_PERIOD
                maxValue = MAX_PERIOD
                wrapSelectorWheel = false
            }

            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = stopwatchAdapter
            }

            addStopwatchButton.setOnClickListener {
                val period = numberPicker.value.toLong().toMilliseconds()
                val stopwatch = Stopwatch(nextId++, period, period, false)
                stopwatchList.add(stopwatch)
                stopwatchAdapter.submitList(stopwatchList.toList())
            }
        }
    }

    private fun Long.toMilliseconds() = (this * 60 * 1000)

    fun onAppBackgrounded() {
        val leftMs = stopwatchList.find { it.id == activeId }?.leftMs
        if (leftMs != null) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(LEFT_TIME_MS, leftMs)
            startService(startIntent)
        }
    }

    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private companion object {
        private const val MIN_PERIOD = 1
        private const val MAX_PERIOD = 25
    }
}