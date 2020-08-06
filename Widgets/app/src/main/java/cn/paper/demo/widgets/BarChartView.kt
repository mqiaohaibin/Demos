package cn.paper.demo.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.annotation.ColorInt
import androidx.annotation.Px

class BarChartView : View {

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var entries: Array<CharSequence>? = null
    private var entryValues: FloatArray? = null

    private var entryWidth: Float = 0f
    private var entryWidthTotal: Float = 0f
    private var entryHeight: Float = 0f

    private val entryRect = Rect()
    private val entryFontMatrix = Paint.FontMetrics()

    private var mActivePointerId: Int = -1
    private var mLastMotionX = 0

    private lateinit var mScroller: OverScroller
    private var mVelocityTracker: VelocityTracker? = null

    private var mMinimumVelocity = 0
    private var mMaximumVelocity = 0
    private var mOverscrollDistance = 0

    private var mOverflingDistance = 0
    private var mHorizontalScrollFactor = 0f
    private var mVerticalScrollFactor = 0f

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        initView()
        initWithAttrs(context, attributeSet)
    }

    private fun initView() {
        axisPaint.color = AXIS_COLOR_DEFAULT
        axisPaint.strokeWidth = AXIS_WIDTH_DEFUALT

        textPaint.textSize = TEXT_SIZE_DEFAULT

        barPaint.color = BAR_COLOR_DEFAULT
        barPaint.strokeWidth = BAR_WIDTH_DEFAULT
        barPaint.style = Paint.Style.FILL

        mScroller = OverScroller(context)

        val configuration = ViewConfiguration.get(context)
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mOverflingDistance = configuration.scaledOverflingDistance
        mOverscrollDistance = configuration.scaledOverscrollDistance

        mHorizontalScrollFactor = configuration.scaledHorizontalScrollFactor
        mVerticalScrollFactor = configuration.scaledVerticalScrollFactor
    }

    private fun initWithAttrs(context: Context, attributeSet: AttributeSet?) {
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.BarChartView, 0, 0).apply {
            try {
                axisColor = getColor(R.styleable.BarChartView_axisColor, axisPaint.color)
                axisWidth =
                    getDimension(R.styleable.BarChartView_axisWidth, axisPaint.strokeWidth)

                val textSize =
                    getDimension(R.styleable.BarChartView_android_textSize, TEXT_SIZE_DEFAULT)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

                barColor = getColor(R.styleable.BarChartView_barColor, barPaint.color)
                barWidth = getDimension(R.styleable.BarChartView_barWidth, barPaint.strokeWidth)
                barStyle = getInt(R.styleable.BarChartView_barStyle, BAR_STYLE_DEFAULT)

                chartWidth = getDimension(R.styleable.BarChartView_chartWidth, CHART_WIDTH_DEFAULT)
                chartSpace = getDimension(R.styleable.BarChartView_chartSpace, CHART_SPACE_DEFAULT)

                val entries = getTextArray(R.styleable.BarChartView_android_entries)
                val resourceId = getResourceId(R.styleable.BarChartView_android_entryValues, 0)
                val entryValues = if (resourceId != 0) {
                    val entryValueTypedArray = resources.obtainTypedArray(resourceId)
                    FloatArray(entryValueTypedArray.length()) { index ->
                        entryValueTypedArray.getFloat(index, 0f)
                    }
                } else {
                    null
                }
                setData(entries, entryValues)

                maxEntryValue = getFloat(R.styleable.BarChartView_maxEntryValue, maxEntryValue)
            } finally {
                recycle()
            }
        }
    }

    private fun getScrollRange(): Int = getContentWidth() - (width - paddingLeft - paddingRight)

    private fun getContentWidth() =
        Math.max(entryWidthTotal.toInt(), width - paddingLeft - paddingRight)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }

                mActivePointerId = event.getPointerId(0)
                mLastMotionX = event.x.toInt()

                initOrResetVelocityTracker()
                mVelocityTracker?.addMovement(event)
            }

            MotionEvent.ACTION_MOVE -> {
                initVelocityTrackerIfNotExists()
                mVelocityTracker?.addMovement(event)
                val deltaX = mLastMotionX - event.x.toInt()
                val clamped = overScrollBy(
                    deltaX, 0, scrollX, 0, getScrollRange(), 0,
                    mOverscrollDistance, 0, true
                )
                if (clamped) {
                    mVelocityTracker?.clear()
                }
                mLastMotionX = event.x.toInt()
            }

            MotionEvent.ACTION_UP -> {
                val velocityTracker = mVelocityTracker
                if (velocityTracker != null) {
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val initialVelocity = velocityTracker.getXVelocity(mActivePointerId).toInt()
                    mScroller.fling(
                        scrollX, scrollY, -initialVelocity, 0, 0, getContentWidth(), 0,
                        0, width / 2, 0
                    )
                    postInvalidateOnAnimation()
                }
                recycleVelocityTracker()
            }

        }
        return true
    }

    override fun computeHorizontalScrollRange(): Int {
        var scrollRange = getScrollRange()
        val contentWidth: Int = width - paddingLeft - paddingRight

        val scrollX: Int = scrollX
        val overscrollRight = Math.max(0, scrollRange - contentWidth)
        if (scrollX < 0) {
            scrollRange -= scrollX
        } else if (scrollX > overscrollRight) {
            scrollRange += scrollX - overscrollRight
        }

        return scrollRange
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        scrollTo(scrollX, scrollY)
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            val oldX: Int = scrollX
            val oldY: Int = scrollY
            val x = mScroller.currX
            val y = mScroller.currY

            if (oldX != x || oldY != y) {
                overScrollBy(
                    x - oldX, y - oldY, oldX, oldY, getScrollRange(), 0,
                    mOverflingDistance, 0, false
                )
                onScrollChanged(scrollX, scrollY, oldX, oldY)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAxis(canvas)
        drawData(canvas)
    }

    private fun drawAxis(canvas: Canvas) {
        val contentWidth = getContentWidth()
        val contentHeight = height.toFloat() - paddingTop - paddingBottom
        val halfPaintStrokeWidth = axisPaint.strokeWidth / 2

        val bottom = paddingTop + contentHeight - entryHeight
        canvas.drawLine(
            paddingLeft.toFloat(),
            bottom - halfPaintStrokeWidth,
            paddingLeft + contentWidth.toFloat(),
            bottom - halfPaintStrokeWidth,
            axisPaint
        )
        canvas.drawLine(
            paddingLeft + halfPaintStrokeWidth,
            bottom - halfPaintStrokeWidth * 2,
            paddingLeft + halfPaintStrokeWidth,
            paddingTop.toFloat(),
            axisPaint
        )
    }

    private fun drawData(canvas: Canvas) {
        val contentHeight = height.toFloat() - paddingTop - paddingBottom

        var x = paddingLeft.toFloat() + chartSpace
        var y = paddingTop + contentHeight - entryFontMatrix.bottom
        entries?.forEachIndexed { index, charSequence ->
            // 1. draw text
            entryRect.setEmpty()
            textPaint.getTextBounds(charSequence, 0, charSequence.length, entryRect)
            val offset = (entryWidth - entryRect.width()) / 2f
            canvas.drawText(charSequence.toString(), x + offset, y, textPaint)

            // 2. draw chart
            val left = x + (entryWidth - chartWidth) / 2f
            val right = left + chartWidth
            val chartHeight = contentHeight - entryHeight - axisPaint.strokeWidth
            val value = (entryValues?.get(index) ?: 0f) / maxEntryValue
            var bottom = paddingTop + chartHeight
            var top = bottom - value * chartHeight
            canvas.drawRect(left, top, right, bottom, barPaint)

            x += entryWidth + chartSpace
        }
    }

    fun setData(entries: Array<CharSequence>?, entryValues: FloatArray?) {
        if (entries?.size == entryValues?.size) {
            this.entries = entries
            this.entryValues = entryValues
            maxEntryValue = Math.max(maxEntryValue, entryValues?.max() ?: 1f)
            resetEntryRect()
            invalidate()
        }
    }

    private fun resetEntryRect() {
        entryWidth = chartWidth
        entryWidthTotal = chartSpace
        entries?.forEach {
            entryRect.setEmpty()
            textPaint.getTextBounds(it, 0, it.length, entryRect)
            entryWidth = Math.max(entryRect.width().toFloat(), entryWidth)
        }
        entryWidthTotal += ((entries?.size ?: 0).toFloat() * (entryWidth + chartSpace))

        textPaint.getFontMetrics(entryFontMatrix)
        entryHeight = Math.max(0f, entryFontMatrix.bottom - entryFontMatrix.top)
    }

    fun setTextSize(size: Float) = setTextSize(TypedValue.COMPLEX_UNIT_SP, size)

    fun setTextSize(unit: Int, size: Float) {
        val resource = context.resources
        val value = TypedValue.applyDimension(unit, size, resource.displayMetrics)
        if (textPaint.textSize != value) {
            textPaint.textSize = value
            resetEntryRect()
            invalidate()
        }
    }

    var axisColor: Int
        set(@ColorInt
            value) {
            if (axisPaint.color != value) {
                axisPaint.color = value
                invalidate()
            }
        }
        get():@ColorInt Int = axisPaint.color

    var axisWidth: Float
        set(@Px value) {
            if (axisPaint.strokeWidth != value) {
                axisPaint.strokeWidth = value
                invalidate()
            }
        }
        get(): @Px Float = axisPaint.strokeWidth

    var barColor: Int
        set(@ColorInt
            value) {
            if (barPaint.color != value) {
                barPaint.color = value
                invalidate()
            }
        }
        get():@ColorInt Int = barPaint.color

    var barWidth: Float
        set(@Px value) {
            if (barPaint.strokeWidth != value) {
                barPaint.strokeWidth = value
                invalidate()
            }
        }
        get(): @Px Float = barPaint.strokeWidth

    var barStyle: Int
        set(value) {
            val style = Paint.Style.values()[value]
            if (barPaint.style != style) {
                barPaint.style = style
                invalidate()
            }
        }
        get() = barPaint.style.ordinal

    var maxEntryValue: Float = 1f

    @Px
    var chartWidth: Float = CHART_WIDTH_DEFAULT
        set(value) {
            if (field != value) {
                field = value
                resetEntryRect()
                invalidate()
            }
        }

    @Px
    var chartSpace: Float = CHART_SPACE_DEFAULT
        set(value) {
            if (field != value) {
                field = value
                resetEntryRect()
                invalidate()
            }
        }

    private fun initOrResetVelocityTracker() {
        val velocityTracker = mVelocityTracker
        if (velocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            velocityTracker.clear()
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    companion object {
        private const val TAG = "BarChartView"

        const val AXIS_COLOR_DEFAULT = Color.BLACK
        const val AXIS_WIDTH_DEFUALT = 5f

        const val TEXT_SIZE_DEFAULT = 10f

        const val BAR_COLOR_DEFAULT = Color.BLACK
        const val BAR_WIDTH_DEFAULT = 5f
        const val BAR_STYLE_DEFAULT = 2

        const val CHART_SPACE_DEFAULT = 10f
        const val CHART_WIDTH_DEFAULT = 40f
    }
}