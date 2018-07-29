package me.cyber.nukleos.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager

/**
 * Билдер менеджеров, что бы избежать передач контекстов во вьюмодели
 */
sealed class RecyclerLayoutManagerBuilder {
    /**
     * Этот метод вызывается в binding adapter'e и получает на вход контекст ресайклера
     */
    abstract fun build(context: Context): RecyclerView.LayoutManager

    class Linear(@RecyclerView.Orientation val orientation: Int = RecyclerView.VERTICAL,
                 val reverseLayout: Boolean = false) : RecyclerLayoutManagerBuilder() {
        override fun build(context: Context) = LinearLayoutManager(context, orientation, reverseLayout)
    }

    class StaggeredGrid(val spanCount: Int, val orientation: Int = StaggeredGridLayoutManager.VERTICAL) : RecyclerLayoutManagerBuilder() {
        override fun build(context: Context) = StaggeredGridLayoutManager(spanCount, orientation)
    }
}