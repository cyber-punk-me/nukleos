package me.cyber.nukleos.ui.find

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_scan_device.*
import me.cyber.nukleos.BaseFragment
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.ui.MainActivity
import me.cyber.nukleos.ui.control.SensorModel
import me.cyber.nukleos.utils.DeviceAdapter
import me.cyber.nukleos.utils.DeviceSelectedListener
import me.cyber.nukleos.utils.RecyclerItemFadeAnimator
import me.cyber.nukleos.utils.showShortToast
import me.cyber.nukleus.R
import javax.inject.Inject

class FindSensorFragment : BaseFragment<FindSensorInterface.Presenter>(), FindSensorInterface.View {

    companion object {
        fun newInstance() = FindSensorFragment()
    }

    @Inject
    lateinit var scanDevicePresenter: FindBluetoothPresenter

    private val mListDeviceAdapter by lazy {
        DeviceAdapter(object : DeviceSelectedListener {
            override fun onDeviceSelected(v: View, position: Int) {
                scanDevicePresenter.onSensorSelected(position)
            }
        })
    }

    override fun isStartupFragment() = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_scan_device, container, false).apply { setHasOptionsMenu(true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab_scan.setOnClickListener { scanDevicePresenter.onFindButtonClicked() }
        with(list_device_found) {
            layoutManager = LinearLayoutManager(this.context)
            itemAnimator = RecyclerItemFadeAnimator()
            adapter = mListDeviceAdapter
        }

        scanDevicePresenter.onFindButtonClicked()
    }
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        attachPresenter(scanDevicePresenter)
        super.onAttach(context)
    }

    override fun showEmptyListText() = with(text_empty_list) {
        text = getString(R.string.sensor_is_not_found)
        visibility = View.VISIBLE
    }

    override fun showPreparingText() = with(text_empty_list) {
        text = getString(R.string.first_do_a_scan)
        visibility = View.VISIBLE
    }

    override fun hideEmptyListText() {
        text_empty_list.visibility = View.INVISIBLE
    }

    override fun populateSensors(sensors: Map<Long, Sensor>) = with(mListDeviceAdapter) {
        deviceList.removeIf { !sensors.contains(it.id) }
        val newSensorModels = sensors.
                filter { sensorPair -> deviceList.all { it.id != sensorPair.key } }.
                map { SensorModel(it.value.name, it.value.address, it.key) }
        deviceList.addAll(newSensorModels)
        notifyDataSetChanged()
    }

    override fun addSensorToList(sensorModel: SensorModel) = with(mListDeviceAdapter) {
        deviceList.add(sensorModel)
        notifyItemInserted(itemCount)
    }

    override fun getSensorModel(index: Int): SensorModel? = with(mListDeviceAdapter) {
        return@with deviceList[index]
    }

    override fun clearSensorList() = with(mListDeviceAdapter) {
        deviceList = mutableListOf()
        notifyItemRangeRemoved(0, itemCount)
    }

    override fun showFindLoader() {
        fab_scan.setImageDrawable(context?.getDrawable(R.drawable.ic_stop))
        progress_bar_search.animate().alpha(1.0f)
    }

    override fun hideFindLoader() {
        fab_scan?.setImageDrawable(context?.getDrawable(R.drawable.abc_ic_search_api_material))
        progress_bar_search?.animate()?.alpha(0.0f)
    }

    override fun showFindError(reason: String?) = showFindErrorToast(reason ?: getString(R.string.unknown_problem))
    override fun showFindSuccess() = getString(R.string.scan_completed).showShortToast()
    override fun goToSensorControl() = (activity as MainActivity).navigateToPage(1)

    private fun showFindErrorToast(reason: String) = "${getString(R.string.scan_failed)}: $reason".showShortToast()
}