package me.cyber.nukleos.ui.dashboard

import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.nitrico.lastadapter.ItemType
import com.github.nitrico.lastadapter.LastAdapter
import me.cyber.nukleos.BR
import me.cyber.nukleos.R
import me.cyber.nukleos.databinding.SensorItemLayoutBinding
import me.cyber.nukleos.extensions.randomFloat
import me.cyber.nukleos.extensions.safeDispose
import me.cyber.nukleos.extensions.shortSubscription
import me.cyber.nukleos.views.RecyclerLayoutManagerBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit


class DashboardViewModel {
    private val mList: ObservableArrayList<Any> = ObservableArrayList()
    val managerBuilder = RecyclerLayoutManagerBuilder.Linear(RecyclerView.VERTICAL)
//    val decoration = Decoration(R.dimen.map_people_nearby_item_decorator_padding.getDimen().toInt())
    val animationDuration = 308
    val adapter = LastAdapter(list = mList, variable = BR.viewModel)
            .type { item, _ ->
                when (item) {
                    is SensorListItemViewModel -> ItemType<SensorItemLayoutBinding>(R.layout.sensor_item_layout)
                    else -> null
                }
            }
    private val mRandom by lazy { Random() }
    private var mListForFirstChart = mutableListOf<Entry>()

    val firstChartData = ObservableField<LineDataSet>(updateChart(10f))
    private var mFirstLastPoint = 0f

    private var writeCharts: Disposable? = null

    private fun updateChart(value: Float): LineDataSet {
        with(mListForFirstChart) {
            add(Entry(mFirstLastPoint++, value))
            sortWith(compareBy({ it.x }, { it.y }))
        }

        return LineDataSet(mListForFirstChart, "firstSensor").apply {
            setColor(Color.RED);
            setValueTextColor(Color.BLUE)
        }
    }

    init {
        mList.add(SensorListItemViewModel("Первый, нах"))
        mList.add(SensorListItemViewModel("Второй, нах"))
        mList.add(SensorListItemViewModel("Третий, нах"))
        mList.add(SensorListItemViewModel("Четвертый, нах"))
        mList.add(SensorListItemViewModel("Пятый, нах"))
        mList.add(SensorListItemViewModel("Шестой, нах"))
        mList.add(SensorListItemViewModel("Седьмой, нах"))
        mList.add(SensorListItemViewModel("Восьмой, нах"))
        mList.add(SensorListItemViewModel("Девятый, нах"))
        mList.add(SensorListItemViewModel("Десятый, нах"))
        writeCharts = Observable.interval(300L, TimeUnit.MILLISECONDS)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .shortSubscription({
                    firstChartData.set(updateChart((-128..128).randomFloat(mRandom)))
                })
    }

    fun release() = writeCharts.safeDispose()
}