package com.easyflow.diarycourse.core.utils.alarm

import com.easyflow.diarycourse.domain.models.ScheduleItem

interface AlarmScheduler {
    fun schedule(alarmItem: ScheduleItem)
    fun cancel(alarmItem: ScheduleItem)
}