package com.easyflow.diarycourse.features.feature_home.schedule.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_home.schedule.ScheduleViewModel
import com.easyflow.diarycourse.features.feature_home.schedule.dialogs.ScheduleItemBottomSheetFragment
import com.easyflow.diarycourse.features.feature_home.schedule.utils.Color
import com.easyflow.diarycourse.features.feature_home.schedule.utils.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleAdapter(private val adapterList: MutableList<ScheduleItem>, private val viewModel: ScheduleViewModel, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<ScheduleAdapter.StatisticViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return StatisticViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: StatisticViewHolder, position: Int) {
        val item = adapterList[position]
        val colorMap: Map<Color, Int> = mapOf(
            Color.BLUE to ContextCompat.getColor(holder.itemView.context, R.color.blue),
            Color.GREEN to ContextCompat.getColor(holder.itemView.context, R.color.green),
            Color.YELLOW to ContextCompat.getColor(holder.itemView.context, R.color.yellow),
            Color.RED to ContextCompat.getColor(holder.itemView.context, R.color.redDialog),
            Color.PURPLE to ContextCompat.getColor(holder.itemView.context, R.color.purple),
            Color.PINK to ContextCompat.getColor(holder.itemView.context, R.color.pink)
        )
        val itemColorInt = colorMap[item.color]

        val density = holder.itemView.context.resources.displayMetrics.density
        val dpToPx = { dp: Float -> (dp * density + 0.5f).toInt() }

        val layoutParams = holder.scheduleItem.layoutParams as ViewGroup.MarginLayoutParams
        if (position == itemCount - 1) {
            layoutParams.bottomMargin = dpToPx(120f)
        } else {
            layoutParams.bottomMargin = dpToPx(10f)
        }
        holder.scheduleItem.layoutParams = layoutParams

        with(holder) {
            startTimeTextView.text = item.startTime
            endTimeTextView.text = item.endTime
            contentTextView.text = item.text
            durationTextView.text = item.duration
            priorityTextView.text = getPriorityString(item.priority)

            if (item.priority == Priority.IMPORTANT) {
                val primaryColor = ContextCompat.getColor(holder.itemView.context, R.color.primary)
                val flagActive = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_flag_small_active)
                priorityTextView.setTextColor(primaryColor)
                priorityIcon.setImageDrawable(flagActive)
            } else {
                val grayColor = ContextCompat.getColor(holder.itemView.context, R.color.textGray)
                val flagInactive = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_flag_small)
                priorityTextView.setTextColor(grayColor)
                priorityIcon.setImageDrawable(flagInactive)
            }
            color.backgroundTintList = ColorStateList.valueOf(itemColorInt ?: ContextCompat.getColor(holder.itemView.context, R.color.blue))
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
                CoroutineScope(Dispatchers.IO).launch {
                    val updatedItem = item.copy(isCompleteTask = !item.isCompleteTask)
                    viewModel.updateData(data = updatedItem)

                    viewModel.update.collect { result: Resource ->
                        when (result) {
                            is Resource.Success -> onSuccess(item, holder)
                            is Resource.Empty.Failed -> onFailed(it)
                        }
                    }
                }
            }
        }
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
        Toast.makeText(itemView.context, "Ошибка завершения", Toast.LENGTH_SHORT).show()
    }

    private fun showBottomSheet(item: ScheduleItem, holder: StatisticViewHolder) {
        val bottomSheetFragment = ScheduleItemBottomSheetFragment(viewModel, fragmentManager)

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
        val contentWrapper: LinearLayout = itemView.findViewById(R.id.contentWrapper)
        val startTimeTextView: TextView = itemView.findViewById(R.id.start_time)
        val endTimeTextView: TextView = itemView.findViewById(R.id.end_time)
        val contentTextView: TextView = itemView.findViewById(R.id.schedule_text)
        val durationTextView: TextView = itemView.findViewById(R.id.schedule_duration)
        val color: LinearLayout = itemView.findViewById(R.id.schedule_oval_background)
        val priorityIcon: ImageView = itemView.findViewById(R.id.priorityIcon)
        val priorityTextView: TextView = itemView.findViewById(R.id.priorityText)
        val isCompleteButton: ImageButton = itemView.findViewById(R.id.complete_schedule_button)
    }

}