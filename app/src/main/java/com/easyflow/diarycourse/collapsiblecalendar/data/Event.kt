package com.easyflow.diarycourse.collapsiblecalendar.data;

class Event(private val mYear: Int, private val mMonth: Int, private val mDay: Int, private val mColor: Int = 0) {
    fun getMonth() = mMonth
    fun getYear() = mYear
    fun getDay() = mDay
    fun getColor() = mColor
}

