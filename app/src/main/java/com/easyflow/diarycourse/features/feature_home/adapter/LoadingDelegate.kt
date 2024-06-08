package com.easyflow.diarycourse.features.feature_home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.easyflow.diarycourse.core.composite.CompositeDelegate
import com.easyflow.diarycourse.core.composite.CompositeItem
import com.easyflow.diarycourse.databinding.TaskLoadingItemBinding

class LoadingDelegate : CompositeDelegate<TaskListItem.Loading, TaskLoadingItemBinding>() {

    override fun canUseForViewType(item: CompositeItem) = item is TaskListItem.Loading

    override fun provideBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = TaskLoadingItemBinding.inflate(inflater, parent, false)

    override fun TaskLoadingItemBinding.bind(item: TaskListItem.Loading) = Unit
}