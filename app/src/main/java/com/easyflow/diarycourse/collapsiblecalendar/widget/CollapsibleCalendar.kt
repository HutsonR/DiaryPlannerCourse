package com.easyflow.diarycourse.collapsiblecalendar.widget

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.collapsiblecalendar.data.CalendarAdapter
import com.easyflow.diarycourse.collapsiblecalendar.data.Day
import com.easyflow.diarycourse.collapsiblecalendar.data.Event
import com.easyflow.diarycourse.collapsiblecalendar.view.BounceAnimator
import com.easyflow.diarycourse.collapsiblecalendar.view.ExpandIconView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.GregorianCalendar


class CollapsibleCalendar : UICalendar, View.OnClickListener {
    private var mAdapter: CalendarAdapter? = null
    private var mListener: CalendarListener? = null
    private var expanded = false
    private var mInitHeight = 0
    private val mHandler = Handler()
    private var mIsWaitingForUpdate = false
    private var mCurrentWeekIndex = 0
    private var reloadJob: Job? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val cal = Calendar.getInstance()
        val adapter = CalendarAdapter(context, cal)
        setAdapter(adapter)

        // Bind events
        mBtnPrevMonth.setOnClickListener { prevMonth() }
        mBtnNextMonth.setOnClickListener { nextMonth() }
        mBtnPrevWeek.setOnClickListener { prevWeek() }
        mBtnNextWeek.setOnClickListener { nextWeek() }
        mTodayIcon.setOnClickListener { changeToToday() }
        expandIconView.setState(ExpandIconView.MORE, true)
        expandIconView.setOnClickListener {
            if (expanded) {
                collapse(400)
            } else {
                expand(400)
            }
        }
        this.post { collapseTo(mCurrentWeekIndex) }
    }

    override fun changeToToday() {
        val calendar = Calendar.getInstance()
        val calenderAdapter = CalendarAdapter(context, calendar);
        calenderAdapter.mEventList = mAdapter!!.mEventList
        calenderAdapter.setFirstDayOfWeek(firstDayOfWeek)
        val today = GregorianCalendar()
        this.selectedItem = null
        this.selectedItemPosition = -1
        this.selectedDay = Day(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        mCurrentWeekIndex = suitableRowIndex
        setAdapter(calenderAdapter)
    }

    private val suitableRowIndex: Int
        get() {
            if (selectedItemPosition != -1) {
                val view = mAdapter!!.getView(selectedItemPosition)
                val row = view.parent as TableRow

                return mTableBody.indexOfChild(row)
            } else if (todayItemPosition != -1) {
                val view = mAdapter!!.getView(todayItemPosition)
                val row = view.parent as TableRow

                return mTableBody.indexOfChild(row)
            } else {
                return 0
            }
        }

    val year: Int
        get() = mAdapter!!.calendar.get(Calendar.YEAR)

    val month: Int
        get() = mAdapter!!.calendar.get(Calendar.MONTH)

    /**
     * The date has been selected and can be used on Calender Listener
     */
    var selectedDay: Day? = null
        get() {
            if (selectedItem == null) {
                val cal = Calendar.getInstance()
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val month = cal.get(Calendar.MONTH)
                val year = cal.get(Calendar.YEAR)
                return Day(
                        year,
                        month + 1,
                        day)
            }
            return Day(
                    selectedItem!!.year,
                    selectedItem!!.month,
                    selectedItem!!.day)
        }
        set(value: Day?) {
            field = value
            redraw()
        }

    var selectedItemPosition: Int = -1
        get() {
            var position = -1
            for (i in 0 until mAdapter!!.count) {
                val day = mAdapter!!.getItem(i)

                if (isSelectedDay(day)) {
                    position = i
                    break
                }
            }
            if (position == -1) {
                position = todayItemPosition
            }
            return position
        }

    val todayItemPosition: Int
        get() {
            var position = -1
            for (i in 0 until mAdapter!!.count) {
                val day = mAdapter!!.getItem(i)

                if (isToday(day)) {
                    position = i
                    break
                }
            }
            return position
        }

    override var state: Int
        get() = super.state
        set(state) {
            super.state = state
            if (state == STATE_COLLAPSED) {
                expanded = false
            }
            if (state == STATE_EXPANDED) {
                expanded = true
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        mInitHeight = mTableBody.measuredHeight

        if (mIsWaitingForUpdate) {
            redraw()
            mHandler.post { collapseTo(mCurrentWeekIndex) }
            mIsWaitingForUpdate = false
            if (mListener != null) {
                mListener!!.onDataUpdate()
            }
        }
    }

    override fun redraw() {
        mAdapter?.let { adapter ->
            for (i in 0 until adapter.count) {
                val day = adapter.getItem(i)
                val view = adapter.getView(i)
                val txtDay = view.findViewById<View>(R.id.txt_day) as TextView
                val dayLayout = view.findViewById<View>(R.id.dayLayout) as LinearLayout
                dayLayout.setBackgroundColor(Color.TRANSPARENT)
                txtDay.setTextColor(textColor)

                // Установка фона для сегодняшнего дня
                if (isToday(day)) {
                    dayLayout.background = todayItemBackgroundDrawable
                    txtDay.setTextColor(todayItemTextColor)
                }

                // Установка фона для выбранного дня
                if (isSelectedDay(day)) {
                    dayLayout.background = selectedItemBackgroundDrawable
                    txtDay.setTextColor(selectedItemTextColor)
                }
            }
        }
    }

    override fun reload() {
        mAdapter?.let { mAdapter ->
            mAdapter.refresh()

            // reset UI
            val month = mAdapter.calendar.get(Calendar.MONTH)
            val year = mAdapter.calendar.get(Calendar.YEAR)
            val monthText = when (month) {
                Calendar.JANUARY -> context.getString(R.string.month_january)
                Calendar.FEBRUARY -> context.getString(R.string.month_february)
                Calendar.MARCH -> context.getString(R.string.month_march)
                Calendar.APRIL -> context.getString(R.string.month_april)
                Calendar.MAY -> context.getString(R.string.month_may)
                Calendar.JUNE -> context.getString(R.string.month_june)
                Calendar.JULY -> context.getString(R.string.month_july)
                Calendar.AUGUST -> context.getString(R.string.month_august)
                Calendar.SEPTEMBER -> context.getString(R.string.month_september)
                Calendar.OCTOBER -> context.getString(R.string.month_october)
                Calendar.NOVEMBER -> context.getString(R.string.month_november)
                Calendar.DECEMBER -> context.getString(R.string.month_december)
                else -> context.getString(R.string.month_unknown)
            }

            val currentMonthText = if (year == Calendar.getInstance().get(Calendar.YEAR)) {
                monthText
            } else {
                "$monthText $year"
            }

            mTxtTitle.text = currentMonthText

            mTableHead.removeAllViews()
            mTableBody.removeAllViews()

            // Установка заголовка недели
            var rowCurrent: TableRow
            rowCurrent = TableRow(context)
            rowCurrent.layoutParams = TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            for (i in 0..6) {
                val view = mInflater.inflate(R.layout.calendar_layout_day_of_week, null)
                val txtDayOfWeek = view.findViewById<View>(R.id.txt_day_of_week) as TextView
                txtDayOfWeek.text = DateFormatSymbols().shortWeekdays[(i + firstDayOfWeek) % 7 + 1]
                view.layoutParams = TableRow.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f)
                rowCurrent.addView(view)
            }
            mTableHead.addView(rowCurrent)

            // Установка дней месяца
            for (i in 0 until mAdapter.count) {

                if (i % 7 == 0) {
                    rowCurrent = TableRow(context)
                    rowCurrent.layoutParams = TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                    mTableBody.addView(rowCurrent)
                }
                val view = mAdapter.getView(i)
                view.layoutParams = TableRow.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f)
                params.let { params ->
                    if (params != null && (mAdapter.getItem(i).getDiff() < params.prevDays || mAdapter.getItem(i).getDiff() > params.nextDaysBlocked)) {
                        view.isClickable = false
                        view.alpha = 0.3f
                    } else {
                        view.setOnClickListener { v -> onItemClicked(v, mAdapter.getItem(i)) }
                    }
                }
                rowCurrent.addView(view)
            }

            redraw()
            mIsWaitingForUpdate = true
        }
    }

    fun onItemClicked(view: View, day: Day) {
        select(day)

        val cal = mAdapter!!.calendar

        val newYear = day.year
        val newMonth = day.month
        val oldYear = cal.get(Calendar.YEAR)
        val oldMonth = cal.get(Calendar.MONTH)
        if (newMonth != oldMonth) {
            cal.set(day.year, day.month, 1)

            if (newYear > oldYear || newMonth > oldMonth) {
                mCurrentWeekIndex = 0
            }
            if (newYear < oldYear || newMonth < oldMonth) {
                mCurrentWeekIndex = -1
            }
            if (mListener != null) {
                mListener!!.onMonthChange()
            }
            reload()
        }

        if (mListener != null) {
            mListener!!.onItemClick(view)
        }
    }

    // Установка адаптера календаря
    fun setAdapter(adapter: CalendarAdapter) {
        mAdapter = adapter
        adapter.setFirstDayOfWeek(firstDayOfWeek)
        reload()
        mCurrentWeekIndex = suitableRowIndex
    }


    fun addEventTag(numYear: Int, numMonth: Int, numDay: Int, color: Int) {
        mAdapter!!.addEvent(Event(numYear, numMonth, numDay, color))

        reloadJob?.cancel()

        // Создаем новый Job с задержкой в 200 миллисекунд
        reloadJob = CoroutineScope(Dispatchers.Main).launch {
            delay(ADD_EVENT_DELAY)
            reload()
        }
    }
    fun prevMonth() {
        val cal = mAdapter!!.calendar
        params?.let {
            val currentYearMonth = Calendar.getInstance().get(Calendar.YEAR) * 12 + Calendar.getInstance().get(Calendar.MONTH)
            val targetYearMonth = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
            if (currentYearMonth + it.prevDays / 30 > targetYearMonth) {
                val myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                val interpolator = BounceAnimator(0.1, 10.0)
                myAnim.interpolator = interpolator
                mTableBody.startAnimation(myAnim)
                mTableHead.startAnimation(myAnim)
                return
            }
        }

        if (cal.get(Calendar.MONTH) == cal.getActualMinimum(Calendar.MONTH)) {
            cal.set(cal.get(Calendar.YEAR) - 1, cal.getActualMaximum(Calendar.MONTH), 1)
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1)
        }
        reload()
        mListener?.onMonthChange()
    }

    fun nextMonth() {
        val cal = mAdapter!!.calendar
        params?.let {
            val currentYearMonth = Calendar.getInstance().get(Calendar.YEAR) * 12 + Calendar.getInstance().get(Calendar.MONTH)
            val targetYearMonth = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
            if (currentYearMonth + it.nextDaysBlocked / 30 < targetYearMonth) {
                val myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                val interpolator = BounceAnimator(0.1, 10.0)
                myAnim.interpolator = interpolator
                mTableBody.startAnimation(myAnim)
                mTableHead.startAnimation(myAnim)
                return
            }
        }

        if (cal.get(Calendar.MONTH) == cal.getActualMaximum(Calendar.MONTH)) {
            cal.set(cal.get(Calendar.YEAR) + 1, cal.getActualMinimum(Calendar.MONTH), 1)
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1)
        }
        reload()
        mListener?.onMonthChange()
    }

    private fun isToday(day: Day?): Boolean {
        val todayCal = Calendar.getInstance()
        return (day != null
                && day.year == todayCal.get(Calendar.YEAR)
                && day.month == todayCal.get(Calendar.MONTH)
                && day.day == todayCal.get(Calendar.DAY_OF_MONTH))
    }

    fun nextDay() {
        if (selectedItemPosition == mAdapter!!.count - 1) {
            nextMonth()
            mAdapter!!.getView(0).performClick()
            reload()
            mCurrentWeekIndex = 0
            collapseTo(mCurrentWeekIndex)
        } else {
            mAdapter!!.getView(selectedItemPosition + 1).performClick()
            if (((selectedItemPosition + 1 - mAdapter!!.calendar.firstDayOfWeek) / 7) > mCurrentWeekIndex) {
                nextWeek()
            }
        }
        mListener?.onDayChanged()
    }

    fun prevDay() {
        if (selectedItemPosition == 0) {
            prevMonth()
            mAdapter!!.getView(mAdapter!!.count - 1).performClick()
            reload()
            return;
        } else {
            mAdapter!!.getView(selectedItemPosition - 1).performClick()
            if (((selectedItemPosition - 1 + mAdapter!!.calendar.firstDayOfWeek) / 7) < mCurrentWeekIndex) {
                prevWeek()
            }
        }
        mListener?.onDayChanged()
    }

    fun prevWeek() {
        if (mCurrentWeekIndex - 1 < 0) {
            mCurrentWeekIndex = -1
            prevMonth()
        } else {
            mCurrentWeekIndex--
            collapseTo(mCurrentWeekIndex)
        }
    }

    fun nextWeek() {
        if (mCurrentWeekIndex + 1 >= mTableBody.childCount) {
            mCurrentWeekIndex = 0
            nextMonth()
        } else {
            mCurrentWeekIndex++
            collapseTo(mCurrentWeekIndex)
        }
    }

    fun isSelectedDay(day: Day?): Boolean {
        return (day != null
                && selectedItem != null
                && day.year == selectedItem!!.year
                && day.month == selectedItem!!.month
                && day.day == selectedItem!!.day)
    }

    /**
     * collapse in milliseconds
     */
    open fun collapse(duration: Int) {
        if (state == STATE_EXPANDED) {
            state = STATE_PROCESSING

            mLayoutBtnGroupMonth.visibility = View.GONE
            mLayoutBtnGroupWeek.visibility = View.VISIBLE
            mBtnPrevWeek.isClickable = false
            mBtnNextWeek.isClickable = false

            val index = suitableRowIndex
            mCurrentWeekIndex = index

            val currentHeight = mInitHeight
            val targetHeight = mTableBody.getChildAt(index).measuredHeight
            var tempHeight = 0
            for (i in 0 until index) {
                tempHeight += mTableBody.getChildAt(i).measuredHeight
            }
            val topHeight = tempHeight

            val anim = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {

                    mScrollViewBody.layoutParams.height = if (interpolatedTime == 1f)
                        targetHeight
                    else
                        currentHeight - ((currentHeight - targetHeight) * interpolatedTime).toInt()
                    mScrollViewBody.requestLayout()

                    if (mScrollViewBody.measuredHeight < topHeight + targetHeight) {
                        val position = topHeight + targetHeight - mScrollViewBody.measuredHeight
                        mScrollViewBody.smoothScrollTo(0, position)
                    }

                    if (interpolatedTime == 1f) {
                        state = STATE_COLLAPSED

                        mBtnPrevWeek.isClickable = true
                        mBtnNextWeek.isClickable = true
                    }
                }
            }
            anim.duration = duration.toLong()
            startAnimation(anim)
        }

        expandIconView.setState(ExpandIconView.MORE, true)
        reload()
    }

    private fun collapseTo(index: Int) {
        var index = index
        if (state == STATE_COLLAPSED) {
            if (index == -1) {
                index = mTableBody.childCount - 1
            }
            mCurrentWeekIndex = index

            val targetHeight = mTableBody.getChildAt(index).measuredHeight
            var tempHeight = 0
            for (i in 0 until index) {
                tempHeight += mTableBody.getChildAt(i).measuredHeight
            }
            val topHeight = tempHeight

            mScrollViewBody.layoutParams.height = targetHeight
            mScrollViewBody.requestLayout()

            mHandler.post { mScrollViewBody.smoothScrollTo(0, topHeight) }


            if (mListener != null) {
                mListener!!.onWeekChange(mCurrentWeekIndex)
            }
        }
    }

    fun expand(duration: Int) {
        if (state == STATE_COLLAPSED) {
            state = STATE_PROCESSING

            mLayoutBtnGroupMonth.visibility = View.VISIBLE
            mLayoutBtnGroupWeek.visibility = View.GONE
            mBtnPrevMonth.isClickable = false
            mBtnNextMonth.isClickable = false

            val currentHeight = mScrollViewBody.measuredHeight
            val targetHeight = mInitHeight

            val anim = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {

                    mScrollViewBody.layoutParams.height = if (interpolatedTime == 1f)
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    else
                        currentHeight - ((currentHeight - targetHeight) * interpolatedTime).toInt()
                    mScrollViewBody.requestLayout()

                    if (interpolatedTime == 1f) {
                        state = STATE_EXPANDED

                        mBtnPrevMonth.isClickable = true
                        mBtnNextMonth.isClickable = true
                    }
                }
            }
            anim.duration = duration.toLong()
            startAnimation(anim)
        }

        expandIconView.setState(ExpandIconView.LESS, true)
        reload()
    }

    fun select(day: Day) {
        selectedItem = Day(day.year, day.month, day.day)

        redraw()

        if (mListener != null) {
            mListener!!.onDaySelect()
        }
    }

    fun setStateWithUpdateUI(state: Int) {
        this@CollapsibleCalendar.state = state

        if (state != state) {
            mIsWaitingForUpdate = true
            requestLayout()
        }
    }

    // Интерфейс слушателя календаря
    interface CalendarListener {
        fun onDaySelect()
        fun onItemClick(v: View)
        fun onDataUpdate()
        fun onMonthChange()
        fun onWeekChange(position: Int)
        fun onClickListener()
        fun onDayChanged()
    }

    // Установка слушателя календаря
    fun setCalendarListener(listener: CalendarListener) {
        mListener = listener
    }

    // Установка видимости иконки раскрытия
    fun setExpandIconVisible(visible: Boolean) {
        expandIconView.visibility = if (visible) View.VISIBLE else View.GONE
    }

    data class Params(val prevDays: Int, val nextDaysBlocked: Int)

    private var params: Params? = null

    override fun onClick(view: View?) {
        view?.let {
            mListener.let { listener ->
                listener?.onClickListener() ?: expandIconView.performClick()
            }
        }
    }

    companion object {
        const val ADD_EVENT_DELAY = 200L
    }
}

