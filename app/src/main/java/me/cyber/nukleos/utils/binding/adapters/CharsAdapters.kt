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