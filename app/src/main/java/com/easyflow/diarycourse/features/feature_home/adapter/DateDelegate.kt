package com.easyflow.diarycourse.features.feature_home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.easyflow.diarycourse.core.composite.CompositeDelegate
import com.easyflow.diarycourse.core.composite.CompositeItem
import com.easyflow.diarycourse.databinding.TaskDateItemBinding

class DateDelegate : CompositeDelegate<TaskListItem.DateHeader, TaskDateItemBinding>() {

    override fun canUseForViewType(item: CompositeItem) = item is TaskListItem.DateHeader

    override fun provideBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = TaskDateItemBinding.inflate(inflater, parent, false)

    override fun TaskDateItemBinding.bind(item: TaskListItem.DateHeader) =
        run { taskDate.text = item.date }
}