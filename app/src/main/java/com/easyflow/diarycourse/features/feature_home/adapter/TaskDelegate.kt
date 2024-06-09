package com.easyflow.diarycourse.features.feature_home.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.composite.CompositeDelegate
import com.easyflow.diarycourse.core.composite.CompositeItem
import com.easyflow.diarycourse.databinding.ScheduleItemBinding
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskDelegate(
    private val onUpdateButtonClick: (id: Int) -> Unit,
    private val onContentClick: (id: String) -> Unit
) : CompositeDelegate<TaskListItem.Task, ScheduleItemBinding>() {

    override fun canUseForViewType(item: CompositeItem) = item is TaskListItem.Task

    override fun provideBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ScheduleItemBinding.inflate(inflater, parent, false)

    override fun ScheduleItemBinding.bind(item: TaskListItem.Task) {
        val context = root.context

        val itemColorInt = getColorForTaskColor(context, item.taskColor)
        val itemColorIntAlpha = getAlphaColorForTaskColor(context, item.taskColor)

        setupTaskViews(item, itemColorInt, itemColorIntAlpha)
        setupTaskActivity(item)

        contentLayout.setOnClickListener {
            onContentClick(item.id)
        }

        completeScheduleButton.setOnClickListener {
            onUpdateButtonClick(item.id.toInt())
        }
    }

    private fun getColorForTaskColor(context: Context, taskColor: TaskColor): Int {
        val taskColorMap: Map<TaskColor, Int> = mapOf(
            TaskColor.BLUE to ContextCompat.getColor(context, R.color.blue),
            TaskColor.GREEN to ContextCompat.getColor(context, R.color.green),
            TaskColor.RED to ContextCompat.getColor(context, R.color.redDialog),
            TaskColor.PURPLE to ContextCompat.getColor(context, R.color.purple),
            TaskColor.PINK to ContextCompat.getColor(context, R.color.pink)
        )
        return taskColorMap[taskColor] ?: ContextCompat.getColor(context, R.color.blue)
    }

    private fun getAlphaColorForTaskColor(context: Context, taskColor: TaskColor): Int {
        val taskColorMapAlpha: Map<TaskColor, Int> = mapOf(
            TaskColor.BLUE to ContextCompat.getColor(context, R.color.blueAlpha),
            TaskColor.GREEN to ContextCompat.getColor(context, R.color.greenAlpha),
            TaskColor.RED to ContextCompat.getColor(context, R.color.redDialogAlpha),
            TaskColor.PURPLE to ContextCompat.getColor(context, R.color.purpleAlpha),
            TaskColor.PINK to ContextCompat.getColor(context, R.color.pinkAlpha)
        )
        return taskColorMapAlpha[taskColor] ?: ContextCompat.getColor(context, R.color.blueAlpha)
    }

    private fun ScheduleItemBinding.setupTaskViews(item: TaskListItem.Task, itemColorInt: Int, itemColorIntAlpha: Int) {
        startTime.text = item.startTime
        endTime.text = item.endTime
        scheduleText.text = item.text
        scheduleDuration.text = item.duration
        priorityText.text = getPriorityString(item.priority)

        taskBackground.backgroundTintList = ColorStateList.valueOf(itemColorIntAlpha)
        completeScheduleButton.setColorFilter(itemColorInt)

        priorityWrapper.visibility = if (item.priority == Priority.STANDARD) View.GONE else View.VISIBLE

        completeScheduleButton.setImageDrawable(
            ContextCompat.getDrawable(
                root.context,
                if (item.isCompleteTask) R.drawable.ic_main_check else R.drawable.ic_main_complete_circle
            )
        )

        if (item.isCompleteTask) {
            scheduleText.paintFlags = scheduleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            contentLayout.alpha = 0.5f
        } else {
            scheduleText.paintFlags = scheduleText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            contentLayout.alpha = 1.0f
        }
    }

    private fun ScheduleItemBinding.setupTaskActivity(item: TaskListItem.Task) {
        // Установка активности задачи
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        val currentTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        scheduleActive.visibility = if (item.startTime <= currentTime && currentDate == item.date && !item.isCompleteTask) {
            if (item.endTime.isNotEmpty() && currentTime > item.endTime) View.INVISIBLE else View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun getPriorityString(priority: Priority): String {
        return when (priority) {
            Priority.STANDARD -> "Обычное"
            Priority.IMPORTANT -> "Важное"
        }
    }

}