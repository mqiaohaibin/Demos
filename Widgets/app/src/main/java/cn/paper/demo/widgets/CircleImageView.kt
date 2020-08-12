package cn.paper.demo.widgets

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Px

class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var drawable: Drawable? = null
        set(value) {
            if (value != field) {
                field = value
                resetShader()

                if (value != null) {
                    var w = value.intrinsicWidth
                    if (w < 0) w = 1
                    var h = value.intrinsicHeight
                    if (h < 0) h = 1
                    if (w != drawableWidth || h != drawableHeight) {
                        drawableWidth = w
                        drawableHeight = h
                        requestLayout()
                    }
                } else {
                    drawableWidth = 0
                    drawableHeight = 0
                    requestLayout()
                }
                invalidate()
            }
        }

    @Px
    private var drawableWidth = 0

    @Px
    private var drawableHeight = 0

    fun resetShader() {
        val drawable = drawable
        if (drawable != null) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            paint.shader =
                BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        } else {
            paint.shader = null
        }
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyleAttr, 0)
            .apply {
                try {
                    drawable = getDrawable(R.styleable.CircleImageView_android_src)
                } finally {
                    recycle()
                }
            }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 0
        var height = 0
        val drawable = drawable
        if (drawable != null) {
            width = drawable.intrinsicWidth
            height = drawable.intrinsicHeight
            if (width < 0) {
                width = 1
            }

            if (height < 0) {
                height = 1
            }
        }

        var measuredWidth = resolveSize(width + paddingLeft + paddingRight, widthMeasureSpec)
        var measuredHeight = resolveSize(height + paddingTop + paddingBottom, heightMeasureSpec)

        var size = Math.max(
            measuredWidth - paddingLeft - paddingRight,
            measuredHeight - paddingTop - paddingBottom
        )

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            size = measuredWidth - paddingLeft - paddingRight
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            size = measuredHeight - paddingTop - paddingBottom
        }

        setMeasuredDimension(
            resolveSize(size + paddingLeft + paddingRight, widthMeasureSpec),
            resolveSize(size + paddingTop + paddingBottom, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentWidth = width.toFloat() - paddingLeft - paddingRight
        val contentHeight = height.toFloat() - paddingTop - paddingBottom

        val size = Math.min(contentWidth, contentHeight)
        val radius = Math.min(drawableWidth / 2f, drawableHeight / 2f)

        val saveCount = canvas.saveCount
        canvas.save()

        val offsetX = (contentWidth - size) / 2f
        val offsetY = (contentHeight - size) / 2f
        canvas.translate(paddingLeft.toFloat() + offsetX, paddingTop.toFloat() + offsetY)

        val scale = size / (radius * 2)
        canvas.scale(scale, scale)

        canvas.translate(radius - drawableWidth / 2f, radius - drawableHeight / 2f)
        canvas.drawCircle(
            drawableWidth.toFloat() / 2f,
            drawableHeight.toFloat() / 2f,
            radius,
            paint
        )

        canvas.restoreToCount(saveCount)
    }

}