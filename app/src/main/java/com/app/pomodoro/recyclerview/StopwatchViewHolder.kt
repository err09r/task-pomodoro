package com.app.pomodoro.recyclerview

import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.pomodoro.R
import com.app.pomodoro.StopwatchListener
import com.app.pomodoro.extensions.Constants.INTERVAL
import com.app.pomodoro.models.Stopwatch
import com.app.pomodoro.databinding.StopwatchItemBinding
import com.app.pomodoro.extensions.displayTime

private const val TAG = "StopwatchViewHolder"

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null
    private val res = binding.root.resources

    fun bind(stopwatch: Stopwatch) {
        binding.apply {
            stopwatchTimer.text = stopwatch.leftMs.displayTime()
            customView.setPeriod(stopwatch.period)
            customView.setCurrent(stopwatch.period - stopwatch.leftMs)
        }
        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer()
        }
        initButtonListeners(stopwatch)
    }

    private fun initButtonListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.deleteButton.setOnClickListener {
            listener.delete(stopwatch.id)
        }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()
        startAnimation()
        binding.startPauseButton.text = res.getString(R.string.stop_button)
    }

    private fun stopTimer() {
        timer?.cancel()
        stopAnimation()
        binding.startPauseButton.text = res.getString(R.string.start_button)
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.leftMs, INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.leftMs -= INTERVAL
                binding.stopwatchTimer.text = millisUntilFinished.displayTime()
                binding.customView.setCurrent(stopwatch.period - stopwatch.leftMs)
            }

            override fun onFinish() {
                stopAnimation()
                stopwatch.apply {
                    isStarted = false
                    stopwatch.leftMs = period
                }
                binding.stopwatchTimer.text = stopwatch.leftMs.displayTime()
                binding.startPauseButton.text = res.getString(R.string.restart_button)
                Toast.makeText(binding.root.context, "Time is over!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startAnimation() {
        val animDrawable = binding.blinkingIndicator.background as? AnimationDrawable
        binding.blinkingIndicator.visibility = View.VISIBLE
        animDrawable?.start()
    }

    private fun stopAnimation() {
        binding.blinkingIndicator.visibility = View.INVISIBLE
        val animDrawable = binding.blinkingIndicator.background as? AnimationDrawable
        animDrawable?.stop()
    }
}