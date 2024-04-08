package com.easyflow.diarycourse.collapsiblecalendar.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TextView
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.collapsiblecalendar.data.Day
import com.easyflow.diarycourse.collapsiblecalendar.view.ExpandIconView
import com.easyflow.diarycourse.collapsiblecalendar.view.LockScrollView
import com.easyflow.diarycourse.collapsiblecalendar.view.OnSwipeTouchListener
import java.util.Locale


@SuppressLint("ClickableViewAccessibility")
abstract class UICalendar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    protected val mInflater: LayoutInflater = LayoutInflater.from(context)

    protected val mLayoutRoot: LinearLayout
    protected val mTxtTitle: TextView
    protected val mTableHead: TableLayout
    protected val mScrollViewBody: LockScrollView
    protected val mTableBody: TableLayout
    protected val mLayoutBtnGroupMonth: LinearLayout
    protected val mLayoutBtnGroupWeek: LinearLayout
    protected val mBtnPrevMonth: TextView
    protected val mBtnNextMonth: TextView
    protected val mBtnPrevWeek: TextView
    protected val mBtnNextWeek: TextView
    protected val expandIconView: ExpandIconView
    protected val clEntireTextView: LinearLayout
    protected val mTodayIcon: ImageView

    var datePattern: String = "MMMM"

    var isShowWeek = true
        set(showWeek) {
            field = showWeek
            mTableHead.visibility = if (showWeek) View.VISIBLE else View.GONE
        }

    var firstDayOfWeek = SUNDAY
        set(firstDayOfWeek) {
            field = firstDayOfWeek
            reload()
        }

    var hideArrow = true
        set(value) {
            field = value
            hideButton()
        }

    open var state = STATE_COLLAPSED
        set(state) {
            field = state
            mLayoutBtnGroupMonth.visibility = if (state == STATE_EXPANDED) View.VISIBLE else View.GONE
            mLayoutBtnGroupWeek.visibility = if (state == STATE_COLLAPSED) View.VISIBLE else View.GONE
        }

    var textColor = Color.BLACK
        set(textColor) {
            field = textColor
            redraw()
            mTxtTitle.setTextColor(this.textColor)
        }

    var primaryColor = Color.WHITE
        set(primaryColor) {
            field = primaryColor
            redraw()
            mLayoutRoot.setBackgroundColor(this.primaryColor)
        }

    var todayItemTextColor = Color.BLACK
        set(todayItemTextColor) {
            field = todayItemTextColor
            redraw()
        }

    var todayItemBackgroundDrawable: Drawable = resources.getDrawable(R.drawable.circle_day_solid_background)

    var selectedItemTextColor = Color.WHITE
        set(selectedItemTextColor) {
            field = selectedItemTextColor
            redraw()
        }

    var selectedItemBackgroundDrawable: Drawable = resources.getDrawable(R.drawable.circle_day_stroke_background)

    private var buttonLeftDrawable: Drawable = resources.getDrawable(R.drawable.ic_left_arrow)

    private var buttonRightDrawable: Drawable = resources.getDrawable(R.drawable.ic_right_arrow)

    var selectedItem: Day? = null

    private var mButtonLeftDrawableTintColor = Color.BLACK
    private var mButtonRightDrawableTintColor = Color.BLACK
    private var mExpandIconColor = Color.BLACK
    private var mEventColor = Color.BLACK

    init {
        val rootView = mInflater.inflate(R.layout.calendar_widget_collapsible_calendarview, this, true)

        mLayoutRoot = rootView.findViewById(R.id.layout_root)
        mTxtTitle = rootView.findViewById(R.id.txt_title)
        mTodayIcon = rootView.findViewById(R.id.today_icon)
        mTableHead = rootView.findViewById(R.id.table_head)
        mTableBody = rootView.findViewById(R.id.table_body)
        mLayoutBtnGroupMonth = rootView.findViewById(R.id.layout_btn_group_month)
        mLayoutBtnGroupWeek = rootView.findViewById(R.id.layout_btn_group_week)
        mBtnPrevMonth = rootView.findViewById(R.id.btn_prev_month)
        mBtnNextMonth = rootView.findViewById(R.id.btn_next_month)
        mBtnPrevWeek = rootView.findViewById(R.id.btn_prev_week)
        mBtnNextWeek = rootView.findViewById(R.id.btn_next_week)
        mScrollViewBody = rootView.findViewById(R.id.scroll_view_body)
        expandIconView = rootView.findViewById(R.id.expandIcon)
        clEntireTextView = rootView.findViewById(R.id.cl_title)

        clEntireTextView.setOnTouchListener { _, _ ->
            expandIconView.performClick()
            true
        }

        mLayoutRoot.setOnTouchListener(getSwipe(context))
        mScrollViewBody.setOnTouchListener(getSwipe(context))
        mScrollViewBody.setParams(getSwipe(context))

        val attributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.UICalendar, defStyleAttr, 0
        )
        setAttributes(attributes)
        attributes.recycle()
    }

    protected abstract fun redraw()
    protected abstract fun reload()
    private fun hideButton() {
        mBtnNextWeek.visibility = View.GONE
        mBtnPrevWeek.visibility = View.GONE
        mBtnNextMonth.visibility = View.GONE
        mBtnPrevMonth.visibility = View.GONE
    }

    protected fun setAttributes(attrs: TypedArray) {
        isShowWeek = attrs.getBoolean(R.styleable.UICalendar_showWeek, isShowWeek)
        firstDayOfWeek = attrs.getInt(R.styleable.UICalendar_firstDayOfWeek, firstDayOfWeek)
        hideArrow = attrs.getBoolean(R.styleable.UICalendar_hideArrows, hideArrow)
        datePattern = attrs.getString(R.styleable.UICalendar_datePattern) ?: datePattern
        state = attrs.getInt(R.styleable.UICalendar_state, state)

        textColor = attrs.getColor(R.styleable.UICalendar_textColor, textColor)
        primaryColor = attrs.getColor(R.styleable.UICalendar_primaryColor, primaryColor)
        mEventColor = attrs.getColor(R.styleable.UICalendar_eventColor, mEventColor)

        todayItemTextColor = attrs.getColor(R.styleable.UICalendar_todayItem_textColor, todayItemTextColor)
        todayItemBackgroundDrawable = attrs.getDrawable(R.styleable.UICalendar_todayItem_background)
            ?: todayItemBackgroundDrawable

        selectedItemTextColor = attrs.getColor(R.styleable.UICalendar_selectedItem_textColor, selectedItemTextColor)
        selectedItemBackgroundDrawable = attrs.getDrawable(R.styleable.UICalendar_selectedItem_background)
            ?: selectedItemBackgroundDrawable

        buttonLeftDrawable = attrs.getDrawable(R.styleable.UICalendar_buttonLeft_drawable) ?: buttonLeftDrawable
        buttonRightDrawable = attrs.getDrawable(R.styleable.UICalendar_buttonRight_drawable) ?: buttonRightDrawable

        setButtonLeftDrawableTintColor(attrs.getColor(R.styleable.UICalendar_buttonLeft_drawableTintColor, mButtonLeftDrawableTintColor))
        setButtonRightDrawableTintColor(attrs.getColor(R.styleable.UICalendar_buttonRight_drawableTintColor, mButtonRightDrawableTintColor))
        setExpandIconColor(attrs.getColor(R.styleable.UICalendar_expandIconColor, mExpandIconColor))
    }

    private fun setButtonLeftDrawableTintColor(color: Int) {
        mButtonLeftDrawableTintColor = color
        mBtnPrevMonth.setTextColor(color)
        mBtnPrevWeek.setTextColor(color)
        redraw()
    }

    private fun setButtonRightDrawableTintColor(color: Int) {
        mButtonRightDrawableTintColor = color
        mBtnNextMonth.setTextColor(color)
        mBtnNextWeek.setTextColor(color)
        redraw()
    }

    private fun setExpandIconColor(color: Int) {
        mExpandIconColor = color
        expandIconView.setColor(color)
    }

    fun getSwipe(context: Context): OnSwipeTouchListener {
        return object : OnSwipeTouchListener(context) {
            override fun onSwipeTop() {
                if (state == STATE_EXPANDED)
                    expandIconView.performClick()
            }

            override fun onSwipeLeft() {
                if (state == STATE_COLLAPSED) {
                    mBtnNextWeek.performClick()
                } else if (state == STATE_EXPANDED) {
                    mBtnNextMonth.performClick()
                }
            }

            override fun onSwipeRight() {
                if (state == STATE_COLLAPSED) {
                    mBtnPrevWeek.performClick()
                } else if (state == STATE_EXPANDED) {
                    mBtnPrevMonth.performClick()
                }
            }

            override fun onSwipeBottom() {
                if (state == STATE_COLLAPSED)
                    expandIconView.performClick()
            }
        }
    }

    fun getCurrentLocale(context: Context): Locale {
        return context.resources.configuration.locales.get(0)
    }

    abstract fun changeToToday()

    companion object {
        const val SUNDAY = 0
        const val MONDAY = 1
        const val TUESDAY = 2
        const val WEDNESDAY = 3
        const val THURSDAY = 4
        const val FRIDAY = 5
        const val SATURDAY = 6

        const val STATE_EXPANDED = 0
        const val STATE_COLLAPSED = 1
        const val STATE_PROCESSING = 2
    }
}