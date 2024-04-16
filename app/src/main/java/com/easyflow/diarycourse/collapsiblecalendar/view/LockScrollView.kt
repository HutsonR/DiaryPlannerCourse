package com.easyflow.diarycourse.collapsiblecalendar.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class LockScrollView : ScrollView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private var swipeTouchListener: OnSwipeTouchListener? = null

    fun setParams(swipeTouchListener: OnSwipeTouchListener){
        this.swipeTouchListener = swipeTouchListener
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return swipeTouchListener?.onTouch(this, ev) ?: false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        return true
    }
}