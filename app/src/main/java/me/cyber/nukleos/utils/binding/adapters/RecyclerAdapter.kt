package me.cyber.nukleos.utils.binding.adapters

import android.databinding.BindingAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.github.nitrico.lastadapter.LastAdapter
import me.cyber.nukleos.views.RecyclerLayoutManagerBuilder

@BindingAdapter(value = ["lastAdapter", "managerBuilder", "animator", "decoration"], requireAll = false)
fun initRecyclerView(rv: RecyclerView, adapter: LastAdapter, managerBuilder: RecyclerLayoutManagerBuilder? = null,
                     animator: RecyclerView.ItemAnimator? = null, decoration: RecyclerView.ItemDecoration? = null) = with(rv) {
    adapter.into(this)
    layoutManager = managerBuilder?.build(this.context) ?: LinearLayoutManager(this.context)
    decoration?.let { addItemDecoration(it) }
    animator?.let { itemAnimator = it }
}