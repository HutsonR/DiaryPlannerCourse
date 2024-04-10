package com.easyflow.diarycourse.collapsiblecalendar.view

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import com.easyflow.diarycourse.R
import kotlin.math.abs

class ExpandIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    @IntDef(MORE, LESS, INTERMEDIATE)
    annotation class State

    @State
    private var state: Int = MORE
    private var alpha: Float = MORE_STATE_ALPHA
    private var centerTranslation: Float = 0f
    @FloatRange(from = 0.0, to = 1.0)
    private var fraction: Float = 0f
    private var animationSpeed: Float = DELTA_ALPHA / DEFAULT_ANIMATION_DURATION

    private var switchColor: Boolean = false
    private var color: Int = Color.BLACK
    private var colorMore: Int = Color.BLACK
    private var colorLess: Int = Color.BLACK
    private var colorIntermediate: Int = -1

    private val paint: Paint
    private val left: Point = Point()
    private val right: Point = Point()
    private val center: Point = Point()
    private val tempLeft: Point = Point()
    private val tempRight: Point = Point()

    private var useDefaultPadding: Boolean = false
    private var padding: Int = 0

    private val path: Path = Path()
    private var arrowAnimator: ValueAnimator? = null

    init {
        val array = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ExpandIconView,
            0, 0
        )

        val roundedCorners: Boolean
        val animationDuration: Long
        try {
            roundedCorners = array.getBoolean(R.styleable.ExpandIconView_eiv_roundedCorners, false)
            switchColor = array.getBoolean(R.styleable.ExpandIconView_eiv_switchColor, false)
            color = array.getColor(R.styleable.ExpandIconView_eiv_color, Color.BLACK)
            colorMore = array.getColor(R.styleable.ExpandIconView_eiv_colorMore, Color.BLACK)
            colorLess = array.getColor(R.styleable.ExpandIconView_eiv_colorLess, Color.BLACK)
            colorIntermediate = array.getColor(R.styleable.ExpandIconView_eiv_colorIntermediate, -1)
            animationDuration = array.getInteger(R.styleable.ExpandIconView_eiv_animationDuration,
                DEFAULT_ANIMATION_DURATION.toInt()
            ).toLong()
            padding = array.getDimensionPixelSize(R.styleable.ExpandIconView_eiv_padding, -1)
            useDefaultPadding = padding == -1
        } finally {
            array.recycle()
        }

        paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            isDither = true
            if (roundedCorners) {
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
        }

        animationSpeed = DELTA_ALPHA / animationDuration
        setState(MORE, false)
    }

    fun setColor(color: Int) {
        this.colorLess = color
        this.colorMore = color
        this.colorIntermediate = color
        invalidate()
    }

    fun switchState() {
        switchState(true)
    }

    fun switchState(animate: Boolean) {
        val newState = when (state) {
            MORE -> LESS
            LESS -> MORE
            INTERMEDIATE -> getFinalStateByFraction()
            else -> throw IllegalArgumentException("Unknown state [$state]")
        }
        setState(newState, animate)
    }

    fun setState(@State state: Int, animate: Boolean) {
        this.state = state
        fraction = if (state == MORE) 0f else 1f
        if (state != INTERMEDIATE) {
            updateArrow(animate)
        }
    }

    fun setFraction(@FloatRange(from = 0.0, to = 1.0) fraction: Float, animate: Boolean) {
        if (fraction < 0f || fraction > 1f) {
            throw IllegalArgumentException("Fraction value must be from 0 to 1f, fraction=$fraction")
        }

        if (this.fraction == fraction) {
            return
        }

        this.fraction = fraction
        state = if (fraction == 0f) MORE else if (fraction == 1f) LESS else INTERMEDIATE

        if (state != INTERMEDIATE) {
            updateArrow(animate)
        }
    }

    fun setAnimationDuration(animationDuration: Long) {
        animationSpeed = DELTA_ALPHA / animationDuration
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(0f, centerTranslation)
        canvas.drawPath(path, paint)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        calculateArrowMetrics(width, height)
        updateArrowPath()
    }

    private fun calculateArrowMetrics(width: Int, height: Int) {
        val arrowMaxWidth = if (height >= width) width else height
        if (useDefaultPadding) {
            padding = (PADDING_PROPORTION * arrowMaxWidth).toInt()
        }
        val arrowWidth = arrowMaxWidth - 2 * padding
        val thickness = arrowWidth * THICKNESS_PROPORTION
        paint.strokeWidth = thickness

        center.set(width / 2, height / 2)
        left.set(center.x - arrowWidth / 2, center.y)
        right.set(center.x + arrowWidth / 2, center.y)
    }

    private fun updateArrow(animate: Boolean) {
        val toAlpha = MORE_STATE_ALPHA + fraction * DELTA_ALPHA
        if (animate) {
            animateArrow(toAlpha)
        } else {
            cancelAnimation()
            alpha = toAlpha
            if (switchColor) {
                updateColor(ArgbEvaluator())
            }
            updateArrowPath()
            invalidate()
        }
    }

    private fun updateArrowPath() {
        path.reset()
        rotate(left, -alpha, tempLeft)
        rotate(right, alpha, tempRight)
        centerTranslation = ((center.y - tempLeft.y) / 2).toFloat()
        path.moveTo(tempLeft.x.toFloat(), tempLeft.y.toFloat())
        path.lineTo(center.x.toFloat(), center.y.toFloat())
        path.lineTo(tempRight.x.toFloat(), tempRight.y.toFloat())
    }

    private fun animateArrow(toAlpha: Float) {
        cancelAnimation()

        val valueAnimator = ValueAnimator.ofFloat(alpha, toAlpha)
        valueAnimator.addUpdateListener { valueAnimator ->
            alpha = valueAnimator.animatedValue as Float
            updateArrowPath()
            if (switchColor) {
                updateColor(ArgbEvaluator())
            }
            postInvalidateOnAnimationCompat()
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = calculateAnimationDuration(toAlpha)
        valueAnimator.start()

        arrowAnimator = valueAnimator
    }

    private fun cancelAnimation() {
        arrowAnimator?.let {
            if (it.isRunning) {
                it.cancel()
            }
        }
    }

    private fun updateColor(colorEvaluator: ArgbEvaluator) {
        val fraction: Float
        val colorFrom: Int
        val colorTo: Int
        fraction = if (colorIntermediate != -1) {
            colorFrom = if (alpha <= 0f) colorMore else colorIntermediate
            colorTo = if (alpha <= 0f) colorIntermediate else colorLess
            if (alpha <= 0) (1 + alpha / 45f) else alpha / 45f
        } else {
            colorFrom = colorMore
            colorTo = colorLess
            (alpha + 45f) / 90f
        }
        color = colorEvaluator.evaluate(fraction, colorFrom, colorTo) as Int
        paint.color = color
    }

    private fun calculateAnimationDuration(toAlpha: Float): Long {
        return (abs(toAlpha - alpha) / animationSpeed).toLong()
    }

    private fun rotate(startPosition: Point, degrees: Float, target: Point) {
        val angle = Math.toRadians(degrees.toDouble())
        val x = (center.x + (startPosition.x - center.x) * Math.cos(angle) -
                (startPosition.y - center.y) * Math.sin(angle)).toInt()

        val y = (center.y + (startPosition.x - center.x) * Math.sin(angle) +
                (startPosition.y - center.y) * Math.cos(angle)).toInt()

        target.set(x, y)
    }

    @State
    private fun getFinalStateByFraction(): Int {
        return if (fraction <= .5f) {
            MORE
        } else {
            LESS
        }
    }

    private fun postInvalidateOnAnimationCompat() {
        postInvalidateOnAnimation()
    }

    companion object {
        private const val MORE_STATE_ALPHA = -45f
        private const val DELTA_ALPHA = 90f
        private const val THICKNESS_PROPORTION = 5f / 36f
        private const val PADDING_PROPORTION = 4f / 24f
        private const val DEFAULT_ANIMATION_DURATION = 150L

        const val MORE = 0
        const val LESS = 1
        private const val INTERMEDIATE = 2
    }
}

