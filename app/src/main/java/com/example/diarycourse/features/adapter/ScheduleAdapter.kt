package com.example.diarycourse.features.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.diarycourse.R
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.features.dialogs.ScheduleItemBottomSheetFragment

class ScheduleAdapter(private val adapterList: MutableList<ScheduleItem>) : RecyclerView.Adapter<ScheduleAdapter.StatisticViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return StatisticViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: StatisticViewHolder, position: Int) {
        val item = adapterList[position]

        holder.startTimeTextView.text = item.startTime
        holder.endTimeTextView.text = item.endTime

        // Установка текста напоминания
        holder.contentTextView.text = item.text
        // Установка длительности напоминания
        holder.durationTextView.text = item.duration

        if (item.isCompleteTask) {
            val checkDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_main_check)
            holder.isCompleteButton.setImageDrawable(checkDrawable)
        } else {
            val circleDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_main_complete_circle)
            holder.isCompleteButton.setImageDrawable(circleDrawable)
        }

        holder.isCompleteButton.setOnClickListener {
            // Изменение значения isCompleteTask
            item.isCompleteTask = !item.isCompleteTask

            // Изменение фона кнопки в зависимости от значения isCompleteTask
            if (item.isCompleteTask) {
                val checkDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_main_check)
                holder.isCompleteButton.setImageDrawable(checkDrawable)
            } else {
                val circleDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_main_complete_circle)
                holder.isCompleteButton.setImageDrawable(circleDrawable)
            }

            // Дополнительно, вы можете уведомить адаптер о том, что данные изменились
            notifyDataSetChanged()
        }
        if (item.isCompleteTask) {
            // Зачеркивание текста
            holder.contentTextView.paintFlags = holder.contentTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            // Установка полупрозрачности
            holder.contentWrapper.alpha = 0.5f
        } else {
            // Отмена зачеркивания текста
            holder.contentTextView.paintFlags = holder.contentTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            // Отмена полупрозрачности
            holder.contentWrapper.alpha = 1.0f
        }

        holder.contentWrapper.setOnClickListener {
            // Show the Bottom Sheet with the entire model
            val bottomSheetFragment = ScheduleItemBottomSheetFragment()

            // Передайте всю модель в аргументы
            val args = Bundle()
            args.putParcelable("scheduleItem", item)
            bottomSheetFragment.arguments = args

            bottomSheetFragment.show(
                (holder.itemView.context as FragmentActivity).supportFragmentManager,
                bottomSheetFragment.tag
            )
        }
    }

    override fun getItemCount(): Int {
        return adapterList.size
    }

    class StatisticViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contentWrapper: LinearLayout = itemView.findViewById(R.id.contentWrapper)
        val startTimeTextView: TextView = itemView.findViewById(R.id.start_time)
        val endTimeTextView: TextView = itemView.findViewById(R.id.end_time)
        val contentTextView: TextView = itemView.findViewById(R.id.schedule_text)
        val durationTextView: TextView = itemView.findViewById(R.id.schedule_duration)
        val isCompleteButton: ImageButton = itemView.findViewById(R.id.complete_schedule_button)
    }

}