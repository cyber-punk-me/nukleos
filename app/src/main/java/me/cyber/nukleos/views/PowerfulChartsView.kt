package me.cyber.nukleos.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import me.cyber.nukleus.R


class PowerfulChartsView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val MAX_SIZE_FOR_DATA = 150
        private const val DEFAULT_CIRCLE_SIZE_ = 3
        private const val CURRENT_CIRCLE_SIZE = 20
    }

    var mChartsCount = 0
        set(value) {
            field = value
            mChartsPointsArray = arrayOf()
            for (i in 0 until value) {
                mChartsPointsArray += FloatArray(MAX_SIZE_FOR_DATA)
            }
        }
    var maxValue = 10.0f
    var minValue = -10.0f
    private val mValueSpace: Float
        get() = maxValue - minValue

    private val mZeroPosition: Float
        get() = (0 - minValue) / mValueSpace

    private val mChartsRectPaint = arrayListOf<Paint>()
    private val mInfoPaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.chart_info)
            textSize = context.resources.getDimensionPixelSize(R.dimen.chart_text_size).toFloat()
            isAntiAlias = true
        }
    }
    var isRunning = false
    private var mChartsPointsArray: Array<FloatArray> = arrayOf()
    private var mPointsArrayCurrentIndex = 0

    init {
        val colorsArray = context.resources.getIntArray(R.array.charts_colors)
        for (i in 0 until colorsArray.size) {
            val paint = Paint()
            paint.color = Color.parseColor("#${Integer.toHexString(colorsArray[i])}")
            mChartsRectPaint += paint
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val requiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> Math.min(requiredHeight, heightSize)
            MeasureSpec.UNSPECIFIED -> requiredHeight
            else -> requiredHeight
        }

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val requiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> Math.min(requiredWidth, widthSize)
            MeasureSpec.UNSPECIFIED -> requiredWidth
            else -> requiredWidth
        }
        setMeasuredDimension(width, height)
    }

    fun addNewPoint(points: FloatArray) {
        for (i in 0 until mChartsCount) {
            this.mChartsPointsArray[i][mPointsArrayCurrentIndex] = (points[i] - minValue) / mValueSpace
        }
        mPointsArrayCurrentIndex = (mPointsArrayCurrentIndex + 1) % MAX_SIZE_FOR_DATA
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = height
        val width = width
        val zeroLine = height - height * mZeroPosition


        canvas.drawLine(0f, zeroLine, width.toFloat(), zeroLine, mInfoPaint)

        if (mChartsPointsArray.isEmpty()) {
            return
        }
        if (!isRunning) {
            return
        }

        val pointSpan: Float = width.toFloat() / MAX_SIZE_FOR_DATA.toFloat()
        var previousX = -1f
        var previousY = -1f
        for (i in 0 until mChartsCount) {
            var currentX = pointSpan

            for (j in 0 until MAX_SIZE_FOR_DATA) {
                val y = height - height * mChartsPointsArray[i][j]
                if (previousX != -1f && previousY != -1f) {
                    canvas.drawLine(previousX, previousY, currentX, y, mChartsRectPaint.get(i))
                }
                if (j == (mPointsArrayCurrentIndex - 1) % MAX_SIZE_FOR_DATA) {
                    canvas.drawCircle(currentX, y, CURRENT_CIRCLE_SIZE.toFloat(), mInfoPaint)
                    previousX = -1f
                    previousY = -1f
                } else {
                    canvas.drawCircle(currentX, y, DEFAULT_CIRCLE_SIZE_.toFloat(), mChartsRectPaint.get(i))
                    previousX = currentX
                    previousY = y
                }
                currentX += pointSpan
            }
            previousX = -1f
            previousY = -1f
        }
    }
}