package com.easyflow.diarycourse.features.feature_calendar.schedule.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_calendar.CalendarViewModel
import com.easyflow.diarycourse.features.feature_calendar.schedule.dialogs.ScheduleItemBottomSheetFragment
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Color
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TimeChangedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleAdapter(private val adapterList: MutableList<ScheduleItem>, private val viewModel: CalendarViewModel, private val activity: FragmentActivity?) : RecyclerView.Adapter<ScheduleAdapter.StatisticViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return StatisticViewHolder(view)
    }

    private suspend fun subscribeToFlow(item: ScheduleItem, holder: StatisticViewHolder, itemView: View) {
        viewModel.result.collect { result ->
            resultCollect(result, item, holder, itemView)
        }
    }

    private suspend fun resultCollect(result: Resource?, item: ScheduleItem, holder: StatisticViewHolder, itemView: View) {
        Log.d("debugTag", "adapter updateCollect $result")
        result?.let {
            when (it) {
                is Resource.Success<*> -> onSuccess(item, holder)
                is Resource.Failed -> onFailed(itemView)
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: StatisticViewHolder, position: Int) {
        val item = adapterList[position]

        // Получение id цвета из Enum
        val colorMap: Map<Color, Int> = mapOf(
            Color.BLUE to ContextCompat.getColor(holder.itemView.context, R.color.blue),
            Color.GREEN to ContextCompat.getColor(holder.itemView.context, R.color.green),
            Color.RED to ContextCompat.getColor(holder.itemView.context, R.color.redDialog),
            Color.PURPLE to ContextCompat.getColor(holder.itemView.context, R.color.purple),
            Color.PINK to ContextCompat.getColor(holder.itemView.context, R.color.pink),
            Color.BLACK to ContextCompat.getColor(holder.itemView.context, R.color.alwaysBlack)
        )
        val itemColorInt = colorMap[item.color]

        // Установка отступа к последнему элементу
        val density = holder.itemView.context.resources.displayMetrics.density
        val dpToPx = { dp: Float -> (dp * density + 0.5f).toInt() }

        val layoutParams = holder.scheduleItem.layoutParams as ViewGroup.MarginLayoutParams
        if (position == itemCount - 1) {
            layoutParams.bottomMargin = dpToPx(120f)
        } else {
            layoutParams.bottomMargin = dpToPx(14f)
        }
        holder.scheduleItem.layoutParams = layoutParams

        with(holder) {
            startTimeTextView.text = item.startTime
            endTimeTextView.text = item.endTime
            contentTextView.text = item.text
            durationTextView.text = item.duration
            priorityTextView.text = getPriorityString(item.priority)
            taskOval.backgroundTintList = ColorStateList.valueOf(itemColorInt ?: ContextCompat.getColor(holder.itemView.context, R.color.blue))
            isCompleteButton.setColorFilter(itemColorInt ?: ContextCompat.getColor(holder.itemView.context, R.color.blue))

            if (item.priority == Priority.STANDARD) {
                priorityWrapper.visibility = View.GONE
            }

            // Установка активности задачи
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            val currentDate = calendar.time.let { dateFormat.format(it) }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val currentTime = String.format("%02d:%02d", hour, minute)

            if (item.startTime <= currentTime && currentDate == item.date && !item.isCompleteTask) {
                if (item.endTime.isNotEmpty() && currentTime > item.endTime) {
                    holder.itemView.post {
                        holder.activeTextView.visibility = View.INVISIBLE
                    }
                } else {
                    holder.itemView.post {
                        holder.activeTextView.visibility = View.VISIBLE
                    }
                }
            } else {
                holder.itemView.post {
                    holder.activeTextView.visibility = View.INVISIBLE
                }
            }

            isCompleteButton.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    if (item.isCompleteTask) R.drawable.ic_main_check else R.drawable.ic_main_complete_circle
                )
            )
            if (item.isCompleteTask) {
                contentTextView.paintFlags = contentTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                contentWrapper.alpha = 0.5f
            } else {
                contentTextView.paintFlags = contentTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                contentWrapper.alpha = 1.0f
            }

            contentWrapper.setOnClickListener {
                showBottomSheet(item, holder)
            }

            isCompleteButton.setOnClickListener {
                Log.d("debugTag", "clicked completeButton")
                val updatedItem = item.copy(isCompleteTask = !item.isCompleteTask)
                CoroutineScope(Dispatchers.IO).launch {
                    subscribeToFlow(item, holder, it)
                }
                sendItemToUpdate(updatedItem)
            }
        }
    }

    private fun sendItemToUpdate(item: ScheduleItem) {
        val bundle = Bundle().apply {
            putParcelable(FRAGMENT_TASK_ITEM, item)
        }
        Log.d("debugTag", "ADAPTER sendItemToUpdate")
        activity?.supportFragmentManager?.setFragmentResult(KEY_ADAPTER_RESULT_UPD, bundle)
    }

    override fun getItemCount(): Int {
        return adapterList.size
    }

    private fun getPriorityString(priority: Priority): String {
        return when (priority) {
            Priority.STANDARD -> "Обычное"
            Priority.IMPORTANT -> "Важное"
        }
    }

    private suspend fun onSuccess(item: ScheduleItem, holder: StatisticViewHolder) {
        Log.d("debugTag", "adapter onSuccess")
        withContext(Dispatchers.Main) {
            item.isCompleteTask = !item.isCompleteTask

            if (item.isCompleteTask) {
                val checkDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_main_check)
                holder.isCompleteButton.setImageDrawable(checkDrawable)
            } else {
                val circleDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_main_complete_circle)
                holder.isCompleteButton.setImageDrawable(circleDrawable)
            }

            notifyDataSetChanged()
        }
    }

    private fun onFailed(itemView: View) {
        Toast.makeText(itemView.context, R.string.error, Toast.LENGTH_SHORT).show()
    }

    private fun showBottomSheet(item: ScheduleItem, holder: StatisticViewHolder) {
        val bottomSheetFragment = ScheduleItemBottomSheetFragment()

        // Передаем всю модель в аргументы
        val args = Bundle()
        args.putParcelable("scheduleItem", item)
        bottomSheetFragment.arguments = args

        bottomSheetFragment.show(
            (holder.itemView.context as FragmentActivity).supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    class StatisticViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val scheduleItem: LinearLayout = itemView.findViewById(R.id.scheduleItem)
        val scheduleIcon: ImageView = itemView.findViewById(R.id.schedule_icon)
        val contentWrapper: LinearLayout = itemView.findViewById(R.id.contentWrapper)
        val startTimeTextView: TextView = itemView.findViewById(R.id.start_time)
        val endTimeTextView: TextView = itemView.findViewById(R.id.end_time)
        val contentTextView: TextView = itemView.findViewById(R.id.schedule_text)
        val durationTextView: TextView = itemView.findViewById(R.id.schedule_duration)
        val taskOval: LinearLayout = itemView.findViewById(R.id.schedule_oval_background)
        val priorityWrapper: LinearLayout = itemView.findViewById(R.id.priorityWrapper)
        val priorityIcon: ImageView = itemView.findViewById(R.id.priorityIcon)
        val priorityTextView: TextView = itemView.findViewById(R.id.priorityText)
        val activeTextView: TextView = itemView.findViewById(R.id.scheduleActive)
        val isCompleteButton: ImageButton = itemView.findViewById(R.id.complete_schedule_button)
    }

    interface ScheduleTimeChangedListener : TimeChangedReceiver.TimeChangedListener {
        override fun onTimeChanged()
    }

    companion object {
        const val KEY_ADAPTER_RESULT_UPD = "KEY_FRAGMENT_RESULT_UPD"

        const val FRAGMENT_TASK_ITEM = "taskItem"
    }

}