package me.cyber.nukleos.views

import android.content.Context
import android.graphics.Paint
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView

class PowerfullBottomNavigationView : BottomNavigationView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var mShiftAmount: Int = 0
    private var mScaleUpFactor: Float = 0f
    private var mScaleDownFactor: Float = 0f
    private var animationRecord: Boolean = false
    private var mLargeLabelSize: Float = 0f
    private var mSmallLabelSize: Float = 0f
    private var visibilityTextSizeRecord: Boolean = false
    private var visibilityHeightRecord: Boolean = false
    private var mItemHeight: Int = 0
    private var textVisibility = false
    private var mMenuView: BottomNavigationMenuView? = null
    private var mButtons: Array<BottomNavigationItemView>? = null

    private val bottomNavigationMenuView: BottomNavigationMenuView?
        get() {
            if (null == mMenuView)
                mMenuView = getField<BottomNavigationMenuView>(BottomNavigationView::class.java, this, "mMenuView")
            return mMenuView
        }

    val bottomNavigationItemViews: Array<BottomNavigationItemView>?
        get() {
            if (null != mButtons)
                return mButtons
            val mMenuView = bottomNavigationMenuView
            mButtons = mMenuView?.let { getField<Array<BottomNavigationItemView>>(mMenuView.javaClass, it, "mButtons") }
            return mButtons
        }

    var itemHeight: Int
        get() {
            val mMenuView = bottomNavigationMenuView
            return mMenuView?.let { getField<Int>(mMenuView.javaClass, it, "mItemHeight") }!!
        }
        set(height) {
            val mMenuView = bottomNavigationMenuView
            mMenuView?.let { setField(mMenuView.javaClass, it, "mItemHeight", height) }
            mMenuView?.updateMenuView()
        }

    fun increaseIcon() = bottomNavigationItemViews?.let {
        for (button in it) {
            val iconView = button.findViewById<View>(android.support.design.R.id.icon)
            val iconSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, resources.displayMetrics).toInt()
            iconView.layoutParams = iconView.layoutParams.apply {
                height = iconSize
                width = iconSize
            }
            bottomNavigationMenuView?.updateMenuView()
        }
    }

    fun setTextVisibility(visibility: Boolean) {
        this.textVisibility = visibility
        val mMenuView = bottomNavigationMenuView
        val mButtons = bottomNavigationItemViews
        for (button in mButtons!!) {
            val mLargeLabel = getField<TextView>(button.javaClass, button, "mLargeLabel")
            val mSmallLabel = getField<TextView>(button.javaClass, button, "mSmallLabel")

            if (!visibility) {
                if (!visibilityTextSizeRecord && !animationRecord) {
                    visibilityTextSizeRecord = true
                    mLargeLabelSize = mLargeLabel!!.textSize
                    mSmallLabelSize = mSmallLabel!!.textSize
                }
                mLargeLabel?.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0f)
                mSmallLabel?.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0f)
            } else {
                if (!visibilityTextSizeRecord)
                    break
                mLargeLabel?.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLargeLabelSize)
                mSmallLabel?.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSmallLabelSize)
            }
        }
        if (!visibility) {
            if (!visibilityHeightRecord) {
                visibilityHeightRecord = true
                mItemHeight = itemHeight
            }
            itemHeight = mItemHeight - getFontHeight(mSmallLabelSize)
        } else {
            if (!visibilityHeightRecord)
                return
            itemHeight = mItemHeight
        }
        mMenuView?.updateMenuView()
    }

    fun enableAnimation(enable: Boolean) {
        val mMenuView = bottomNavigationMenuView
        val mButtons = bottomNavigationItemViews
        for (button in mButtons!!) {
            val mLargeLabel = getField<TextView>(button.javaClass, button, "mLargeLabel")
            val mSmallLabel = getField<TextView>(button.javaClass, button, "mSmallLabel")

            if (!enable) {
                if (!animationRecord) {
                    animationRecord = true
                    mShiftAmount = getField<Int>(button.javaClass, button, "mShiftAmount")!!
                    mScaleUpFactor = getField<Float>(button.javaClass, button, "mScaleUpFactor")!!
                    mScaleDownFactor = getField<Float>(button.javaClass, button, "mScaleDownFactor")!!

                    mLargeLabelSize = mLargeLabel!!.textSize
                    mSmallLabelSize = mSmallLabel!!.textSize

                }
                setField(button.javaClass, button, "mShiftAmount", 0)
                setField(button.javaClass, button, "mScaleUpFactor", 1)
                setField(button.javaClass, button, "mScaleDownFactor", 1)

                mLargeLabel?.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSmallLabelSize)

            } else {
                if (!animationRecord)
                    return
                setField(button.javaClass, button, "mShiftAmount", mShiftAmount)
                setField(button.javaClass, button, "mScaleUpFactor", mScaleUpFactor)
                setField(button.javaClass, button, "mScaleDownFactor", mScaleDownFactor)
                mLargeLabel!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLargeLabelSize)
            }
        }
        mMenuView?.updateMenuView()
    }

    fun enableShiftingMode(enable: Boolean) {
        val mMenuView = bottomNavigationMenuView
        mMenuView?.let { setField(mMenuView.javaClass, it, "mShiftingMode", false) }
        mMenuView?.updateMenuView()
    }

    fun enableItemShiftingMode(enable: Boolean) {
        val mMenuView = bottomNavigationMenuView
        val mButtons = bottomNavigationItemViews
        for (button in mButtons!!) {
            setField(button.javaClass, button, "mShiftingMode", false)
        }
        mMenuView?.updateMenuView()
    }

    private fun <T> getField(targetClass: Class<*>, instance: Any, fieldName: String): T? {
        try {
            val field = targetClass.getDeclaredField(fieldName)
            field.isAccessible = true
            return field.get(instance) as T
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return null
    }

    private fun setField(targetClass: Class<*>, instance: Any, fieldName: String, value: Any) {
        try {
            val field = targetClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(instance, value)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }

    private fun getFontHeight(fontSize: Float): Int {
        val paint = Paint()
        paint.textSize = fontSize
        val fm = paint.fontMetrics
        return Math.ceil((fm.descent - fm.top).toDouble()).toInt() + 2
    }
}