package me.cyber.nukleos.views

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import me.cyber.nukleus.R

class PowerfulStateButton(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val DEFAULT_WIDTH = 90
        private const val DEFAULT_HEIGHT = 60

        private const val DEFAULT_COLOR = Color.GREEN
        private const val DEFAULT_TEXT_COLOR = Color.WHITE
        private const val STATE_BUTTON = 0
        private const val ANIMATION_POSITION_1 = 1
        private const val ANIMATION_POSITION_2 = 2
        private const val ANIMATION_FOR_LOADING = 3
        private const val STOP_LOADING = 4
        private const val ANIMATION_FOR_SUCCESS = 5
        private const val ANIMATION_FOR_FAILED = 6
    }

    private val mDensity = resources.displayMetrics.density
    private var mCurrentState = STATE_BUTTON
    private var mButtonCorner = 2 * mDensity
    private var mRadius = 0
    private var mTextWidth = 0f
    private var mTextHeight = 0f
    private var mScaleWidth = 0
    private var mScaleHeight = 0
    private var mDegree = 0
    private var mAngle = 0
    private var mEndAngle = 0
    private var mRippleRadius = 0f
    private var mSuccessPathLength = 0f
    private var mFailedPathLength = 0f
    private var mFailedPathIntervals: FloatArray? = null
    private var mSuccessPathIntervals: FloatArray? = null

    private var mTouchX = 0f
    private var mTouchY = 0f

    private val mMatrix by lazy { Matrix() }
    private val mPath by lazy { Path() }
    private var mSuccessPath = Path()
    private val mFailedPath by lazy { Path() }
    private val mFailedPath2 by lazy { Path() }
    private val mButtonRectF by lazy { RectF() }
    private val mArcRectF by lazy { RectF() }
    private val mLoadingAnimatorSet by lazy { AnimatorSet() }
    val currentState = mCurrentState

    private var mColorPrimary = DEFAULT_COLOR
    private var mDisabledBgColor = Color.LTGRAY
    private var mTextColor = Color.WHITE
    private var mDisabledTextColor = Color.DKGRAY
    private var mRippleColor = Color.BLACK
    private var mRippleAlpha = 0.3f

    private var mPadding = 6 * mDensity
    private var mText: String = ""
    var animationEndAction: ((AnimationType) -> Unit)? = null


    var textColor
        get() = mTextColor
        set(value) {
            mTextColor = value
            invalidate()
        }

    var typeface: Typeface
        get() = mTextPaint.typeface
        set(value) {
            mTextPaint.typeface = value
            invalidate()
        }

    var text
        get() = mText
        set(value) {
            if (text.isEmpty()) {
                return
            }
            this.mText = value
            mTextWidth = mTextPaint.measureText(mText)
            mTextHeight = measureTextHeight(mTextPaint)
            invalidate()
        }

    var textSize
        get() = (mTextPaint.textSize / mDensity).toInt()
        set(value) {
            mTextPaint.textSize = value * mDensity
            mTextWidth = mTextPaint.measureText(mText)
            invalidate()
        }

    var cornerRadius
        get() = mButtonCorner
        set(value) {
            mButtonCorner = value
            invalidate()
        }

    var resetAfterFailed = true

    var backgroundShader: Shader?
        get() = mStrokePaint.shader
        set(value) {
            mPaint.shader = value
            mStrokePaint.shader = value
            mPathEffectPaint.shader = value
            mPathEffectPaint2.shader = value
            invalidate()
        }

    private val mPaint by lazy {
        Paint().apply {
            setLayerType(View.LAYER_TYPE_SOFTWARE, this)
            isAntiAlias = true
            color = mColorPrimary
            style = Paint.Style.FILL
            setShadow(context, 1)
        }
    }

    private val ripplePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = mRippleColor
            alpha = (mRippleAlpha * 255).toInt()
            style = Paint.Style.FILL
        }
    }

    private val mStrokePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = mColorPrimary
            style = Paint.Style.STROKE
            strokeWidth = 2 * mDensity
        }
    }


    private val mTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = mTextColor
            textSize = 16 * mDensity
            isFakeBoldText = true
        }
    }

    private val mPathEffectPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = mColorPrimary
            style = Paint.Style.STROKE
            strokeWidth = 2 * mDensity
        }
    }
    private val mPathEffectPaint2 by lazy {
        Paint().apply {
            isAntiAlias = true
            color = mColorPrimary
            style = Paint.Style.STROKE
            strokeWidth = 2 * mDensity
        }
    }

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.PowerfulStateButton, 0, 0)
            mColorPrimary = ta.getInt(R.styleable.PowerfulStateButton_btnColor_PowStBtn, Color.GREEN)
            mDisabledBgColor = ta.getColor(R.styleable.PowerfulStateButton_btnDisabledColor_PowStBtn, Color.LTGRAY)
            mDisabledTextColor = ta.getColor(R.styleable.PowerfulStateButton_disabledTextColor_PowStBtn, Color.DKGRAY)
            val text = ta.getString(R.styleable.PowerfulStateButton_btnText_PowStBtn)
            mText = text ?: ""
            mTextColor = ta.getColor(R.styleable.PowerfulStateButton_textColor_PowStBtn, Color.WHITE)
            resetAfterFailed = ta.getBoolean(R.styleable.PowerfulStateButton_resetAfterFailed_PowStBtn, true)
            mRippleColor = ta.getColor(R.styleable.PowerfulStateButton_btnRippleColor_PowStBtn, Color.BLACK)
            mRippleAlpha = ta.getFloat(R.styleable.PowerfulStateButton_btnRippleAlpha_PowStBtn, 0.3f)
            mButtonCorner = ta.getFloat(R.styleable.PowerfulStateButton_cornerRadius_PowStBtn, 2 * mDensity)
            ta.recycle()
        }

        mTextWidth = mTextPaint.measureText(mText)
        mTextHeight = measureTextHeight(mTextPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
                measureDimension((DEFAULT_WIDTH * mDensity).toInt(), widthMeasureSpec),
                measureDimension((DEFAULT_HEIGHT * mDensity).toInt(), heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRadius = (height - mPadding * 2).toInt() / 2

        with(mButtonRectF) {
            top = mPadding
            bottom = height - mPadding
        }
        with(mArcRectF) {
            left = (width / 2 - mRadius).toFloat()
            top = mPadding
            right = (width / 2 + mRadius).toFloat()
            bottom = height - mPadding
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (mCurrentState == STATE_BUTTON) {
            updateButtonColor()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchX = event.x
                mTouchY = event.y
                playRippleAnimation(true)
            }
            MotionEvent.ACTION_UP -> if (event.x > mButtonRectF.left && event.x < mButtonRectF.right && event.y > mButtonRectF.top && event.y < mButtonRectF.bottom) {
                playRippleAnimation(false)
            } else {
                mRippleRadius = 0f
                mTouchX = 0f
                mTouchY = 0f
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        with(canvas) {
            when (mCurrentState) {
                ANIMATION_FOR_LOADING -> {
                    mPath.reset()
                    mPath.addArc(mArcRectF, (270 + mAngle / 2).toFloat(), (360 - mAngle).toFloat())
                    if (mAngle != 0) {
                        mMatrix.setRotate(mDegree.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())
                        mPath.transform(mMatrix)
                        mDegree += 10
                    }
                    drawPath(mPath, mStrokePaint)
                }
                ANIMATION_FOR_SUCCESS -> {
                    drawPath(mSuccessPath, mPathEffectPaint)
                    drawCircle((width / 2).toFloat(), (height / 2).toFloat(), mRadius - mDensity, mStrokePaint)
                }
                ANIMATION_FOR_FAILED -> {
                    drawPath(mFailedPath, mPathEffectPaint)
                    drawPath(mFailedPath2, mPathEffectPaint2)
                    drawCircle((width / 2).toFloat(), (height / 2).toFloat(), mRadius - mDensity, mStrokePaint)
                }
                STOP_LOADING -> {
                    mPath.reset()
                    mPath.addArc(mArcRectF, (270 + mAngle / 2).toFloat(), mEndAngle.toFloat())
                    if (mEndAngle != 360) {
                        mMatrix.setRotate(mDegree.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())
                        mPath.transform(mMatrix)
                        mDegree += 10
                    }
                    drawPath(mPath, mStrokePaint)
                }
                STATE_BUTTON, ANIMATION_POSITION_1 -> {
                    val cornerRadius = (mRadius - mButtonCorner) * (mScaleWidth / (width / 2 - height / 2).toFloat()) + mButtonCorner
                    mButtonRectF.left = mScaleWidth.toFloat()
                    mButtonRectF.right = (width - mScaleWidth).toFloat()
                    drawRoundRect(mButtonRectF, cornerRadius, cornerRadius, mPaint)
                    if (mCurrentState == STATE_BUTTON) {
                        drawText(mText, (width - mTextWidth) / 2, (height - mTextHeight) / 2 + mPadding * 2, mTextPaint)
                        if (mTouchX > 0 || mTouchY > 0) {
                            clipRect(0f, mPadding, width.toFloat(), height - mPadding)
                            drawCircle(mTouchX, mTouchY, mRippleRadius, ripplePaint)
                        }
                    }
                }
                ANIMATION_POSITION_2 -> {
                    drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (mRadius - mScaleHeight).toFloat(), mPaint)
                    drawCircle((width / 2).toFloat(), (height / 2).toFloat(), mRadius - mDensity, mStrokePaint)
                }
            }
        }
    }

    fun startLoading() {
        if (mCurrentState == ANIMATION_FOR_FAILED && !resetAfterFailed) {
            scaleFailedPath()
            return
        }

        if (mCurrentState == STATE_BUTTON) {
            mCurrentState = ANIMATION_POSITION_1
            mPaint.clearShadowLayer()
            playStartAnimation(false)
        }
    }

    fun loadingSuccess() {
        if (mLoadingAnimatorSet.isStarted) {
            mLoadingAnimatorSet.end()
            mCurrentState = STOP_LOADING
            playSuccessAnimation()
        }
    }

    fun loadingFail() {
        if (mLoadingAnimatorSet.isStarted) {
            mLoadingAnimatorSet.end()
            mCurrentState = STOP_LOADING
            playFailedAnimation()
        }
    }

    fun stopLoading() {
        if (mCurrentState != ANIMATION_FOR_LOADING) {
            return
        }
        cancel()
    }

    fun resetAnimation() {
        when (mCurrentState) {
            ANIMATION_FOR_SUCCESS -> scaleSuccessPath()
            ANIMATION_FOR_FAILED -> scaleFailedPath()
        }
    }

    private fun measureTextHeight(paint: Paint): Float {
        val bounds = Rect()
        paint.getTextBounds(mText, 0, mText.length, bounds)
        return bounds.height().toFloat()
    }

    private fun createSuccessPath() {
        mSuccessPath.reset()
        val mLineWith = 2 * mDensity
        mSuccessPath = Path().apply {
            moveTo((width / 2 - mRadius).toFloat() + (mRadius / 3).toFloat() + mLineWith, mPadding + mRadius.toFloat() + mLineWith)
            lineTo((width / 2 - mRadius / 6).toFloat(), (mLineWith + mRadius) * 1.5f + mPadding / 2)
            lineTo((width / 2 + mRadius).toFloat() - mLineWith - (mRadius / 3).toFloat(), mPadding + (mRadius / 2).toFloat() + mLineWith)
        }

        mSuccessPathLength = PathMeasure(mSuccessPath, false).length
        mSuccessPathIntervals = floatArrayOf(mSuccessPathLength, mSuccessPathLength)
    }

    private fun createFailedPath() {
        mFailedPath.reset()
        mFailedPath2.reset()
        val left = (width / 2 - mRadius + mRadius / 2).toFloat()
        val top = mRadius / 2 + mPadding
        with(mFailedPath) {
            moveTo(left, top)
            lineTo(left + mRadius, top + mRadius)
        }
        with(mFailedPath2) {
            moveTo((width / 2 + mRadius / 2).toFloat(), top)
            lineTo((width / 2 - mRadius + mRadius / 2).toFloat(), top + mRadius)
        }

        mFailedPathLength = PathMeasure(mFailedPath, false).length
        mFailedPathIntervals = floatArrayOf(mFailedPathLength, mFailedPathLength)
        mPathEffectPaint2.pathEffect = DashPathEffect(mFailedPathIntervals, mFailedPathLength)
    }

    private fun measureDimension(defaultSize: Int, measureSpec: Int) =
            when (View.MeasureSpec.getMode(measureSpec)) {
                EXACTLY -> View.MeasureSpec.getSize(measureSpec)
                AT_MOST -> Math.min(defaultSize, View.MeasureSpec.getSize(measureSpec))
                UNSPECIFIED -> defaultSize
                else -> defaultSize
            }

    private fun updateButtonColor() {
        mPaint.color = if (isEnabled) mColorPrimary else mDisabledBgColor
        mTextPaint.color = if (isEnabled) mTextColor else mDisabledTextColor
        if (backgroundShader != null) {
            if (isEnabled) mPaint.shader = backgroundShader else mPaint.shader = null
        }
        invalidate()
    }


    private fun playRippleAnimation(isTouchDown: Boolean) {
        mPaint.setShadow(context, 2)
        ValueAnimator.ofFloat(
                if (isTouchDown) 0f else (width / 2).toFloat(),
                if (isTouchDown) (width / 2).toFloat() else width.toFloat()).apply {
            duration = 240
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                mRippleRadius = valueAnimator.animatedValue as Float
                invalidate()
            }
            if (!isTouchDown) endAction {
                performClick()
                mTouchX = 0f
                mTouchY = 0f
                mRippleRadius = 0f
                invalidate()
            }
        }.start()
    }

    private fun playStartAnimation(isReverse: Boolean) {

        val animator = ValueAnimator.ofInt(
                if (isReverse) width / 2 - height / 2 else 0,
                if (isReverse) 0 else width / 2 - height / 2).apply {
            duration = 400
            interpolator = AccelerateDecelerateInterpolator()
            startDelay = 100
            addUpdateListener { valueAnimator ->
                mScaleWidth = valueAnimator.animatedValue as Int
                invalidate()
            }
            endAction {
                mCurrentState = if (isReverse) STATE_BUTTON else ANIMATION_POSITION_2
                if (mCurrentState == STATE_BUTTON) {
                    mPaint.setShadow(context, 1)
                    invalidate()
                }
            }
        }

        val animator2 = ValueAnimator.ofInt(if (isReverse) mRadius else 0, if (isReverse) 0 else mRadius)
                .apply {
                    duration = 240
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        mScaleHeight = valueAnimator.animatedValue as Int
                        invalidate()
                    }
                    endAction {
                        mCurrentState = if (isReverse) ANIMATION_POSITION_1 else ANIMATION_FOR_LOADING
                        if (!isReverse) updateButtonColor()
                    }
                }

        val loadingAnimator = ValueAnimator.ofInt(30, 300)
                .apply {
                    duration = 1000
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.REVERSE
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        mAngle = valueAnimator.animatedValue as Int
                        invalidate()
                    }
                }
        with(mLoadingAnimatorSet) {
            cancel()
            endAction {
                isEnabled = true
                updateButtonColor()
            }
            if (isReverse) {
                playSequentially(animator2, animator)
                start()
                return
            }
            playSequentially(animator, animator2, loadingAnimator)
            start()
        }
    }

    private fun playSuccessAnimation() {
        createSuccessPath()
        val animator = ValueAnimator.ofInt(360 - mAngle, 360).apply {
            duration = 240
            interpolator = DecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                mEndAngle = valueAnimator.animatedValue as Int
                invalidate()
            }
            endAction { mCurrentState = ANIMATION_FOR_SUCCESS }
        }

        val successAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Float
                val pathEffect = DashPathEffect(mSuccessPathIntervals, mSuccessPathLength - mSuccessPathLength * value)
                mPathEffectPaint.pathEffect = pathEffect
                invalidate()
            }
        }

        AnimatorSet().apply {
            playSequentially(animator, successAnimator)
            endAction { animationEndAction?.invoke(AnimationType.SUCCESSFUL) }
        }.start()
    }

    private fun playFailedAnimation() {
        createFailedPath()
        val animator = ValueAnimator.ofInt(360 - mAngle, 360)
                .apply {
                    duration = 240
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        mEndAngle = valueAnimator.animatedValue as Int
                        invalidate()
                    }
                    endAction { mCurrentState = ANIMATION_FOR_FAILED }
                }

        val failedAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
                .apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        mPathEffectPaint.pathEffect = DashPathEffect(mFailedPathIntervals, mFailedPathLength - mFailedPathLength * value)
                        invalidate()
                    }
                }

        val failedAnimator2 = ValueAnimator.ofFloat(0.0f, 1.0f)
                .apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        mPathEffectPaint2.pathEffect = DashPathEffect(mFailedPathIntervals, mFailedPathLength - mFailedPathLength * value)
                        invalidate()
                    }
                }

        AnimatorSet().apply {
            playSequentially(animator, failedAnimator, failedAnimator2)
            endAction {
                if (resetAfterFailed) {
                    postDelayed({ scaleFailedPath() }, 1000)
                } else {
                    animationEndAction?.invoke(AnimationType.FAILED)
                }
            }
        }.start()
    }

    private fun cancel() {
        mCurrentState = STOP_LOADING
        ValueAnimator.ofInt(360 - mAngle, 360)
                .apply {
                    duration = 240
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        mEndAngle = valueAnimator.animatedValue as Int
                        invalidate()
                    }
                    endAction {
                        mCurrentState = ANIMATION_POSITION_2
                        playStartAnimation(true)
                    }
                }.start()
    }

    private fun scaleSuccessPath() {
        val scaleMatrix = Matrix()
        ValueAnimator.ofFloat(1.0f, 0.0f)
                .apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        scaleMatrix.setScale(value, value, (width / 2).toFloat(), (height / 2).toFloat())
                        mSuccessPath.transform(scaleMatrix)
                        invalidate()
                    }
                    endAction {
                        mCurrentState = ANIMATION_POSITION_2
                        playStartAnimation(true)
                    }
                }.start()
    }

    private fun scaleFailedPath() {
        val scaleMatrix = Matrix()
        ValueAnimator.ofFloat(1.0f, 0.0f)
                .apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        scaleMatrix.setScale(value, value, (width / 2).toFloat(), (height / 2).toFloat())
                        mFailedPath.transform(scaleMatrix)
                        mFailedPath2.transform(scaleMatrix)
                        invalidate()
                    }
                    endAction {
                        mCurrentState = ANIMATION_POSITION_2
                        playStartAnimation(true)
                    }
                }.start()
    }
}

enum class AnimationType {
    FAILED, SUCCESSFUL, UNDEFINED
}

private fun Animator.endAction(action: (animator: Animator?) -> Unit) {
    this.addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) = action(animation)
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
    })
}

private fun Paint.setShadow(context: Context, depth: Int) {
    val density = context.resources.displayMetrics.density
    this.setShadowLayer(depth * density, 0f, 2 * density, 0x1F000000)
}