package me.cyber.nukleos.utils.binding.adapters

import android.databinding.BindingAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@BindingAdapter("setDataForChar")
fun setDataForChart(lineChart: LineChart, data: LineDataSet) =
        with(lineChart) {
            setData(LineData(data));
            invalidate()
        }

@BindingAdapter("setAnimationForChar")
fun setAnimation(lineChart: LineChart, duration: Int){
    lineChart.animateY(1000)
    lineChart.animateX(1000)
}