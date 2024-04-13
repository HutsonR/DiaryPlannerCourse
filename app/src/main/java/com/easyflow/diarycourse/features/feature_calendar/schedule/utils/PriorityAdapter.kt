package com.easyflow.diarycourse.features.feature_calendar.schedule.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.easyflow.diarycourse.R

class PriorityAdapter(context: Context, items: List<String>) :
    ArrayAdapter<String>(context, R.layout.custom_spinner_dropdown_item, items) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.task_item_priority, parent, false)
        val priorityItem = getItem(position)
        val textView: TextView = view.findViewById(R.id.priorityPickerText)
        textView.text = priorityItem
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.custom_spinner_dropdown_item, parent, false)
        val customTextView: TextView = view.findViewById(R.id.customTextView)
        customTextView.text = getItem(position)
        return view
    }
}

