package me.cyber.nukleos.views

import android.content.Context
import android.view.MotionEvent
import android.support.v4.view.ViewPager
import android.util.AttributeSet


class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    var pagingEnabled: Boolean = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (pagingEnabled) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (pagingEnabled) {
            super.onInterceptTouchEvent(event)
        } else false
    }

}