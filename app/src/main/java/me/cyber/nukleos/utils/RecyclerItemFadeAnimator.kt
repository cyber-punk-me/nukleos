package me.cyber.nukleos.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView

class RecyclerItemFadeAnimator : DefaultItemAnimator() {

    companion object {
        private const val DURATION = 1000L
        private const val ALPHA_START = 0.0f
        private const val ALPHA_FINISH = 1.0f
    }

    override fun animateAdd(viewHolder: RecyclerView.ViewHolder): Boolean {
        if (viewHolder is DeviceAdapter.DeviceViewHolder) {
            viewHolder.item.alpha = ALPHA_START
            viewHolder.itemView
                    .animate()
                    .alpha(ALPHA_FINISH)
                    .setDuration(DURATION)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            dispatchAddFinished(viewHolder)
                        }
                    })
                    .start()
        }
        return false
    }
}
