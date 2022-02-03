package com.app.pomodoro.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.app.pomodoro.StopwatchListener
import com.app.pomodoro.models.Stopwatch
import com.app.pomodoro.databinding.StopwatchItemBinding

class StopwatchAdapter(private val listener: StopwatchListener) :
    ListAdapter<Stopwatch, StopwatchViewHolder>(AsyncDifferConfig.Builder(itemComparator).build()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopwatchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = StopwatchItemBinding.inflate(inflater, parent, false)
        return StopwatchViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: StopwatchViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun getItemCount(): Int = currentList.size

    private companion object {
        private val itemComparator = object : DiffUtil.ItemCallback<Stopwatch>() {
            override fun areItemsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.leftMs == newItem.leftMs &&
                        oldItem.isStarted == newItem.isStarted &&
                        oldItem.period == newItem.period
            }

            override fun getChangePayload(oldItem: Stopwatch, newItem: Stopwatch) = Any()
        }
    }
}