package me.cyber.nukleos.utils

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleus.R

class DeviceAdapter(private val deviceSelectedListener: DeviceSelectedListener) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    var deviceList = mutableListOf<Sensor>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DeviceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false), deviceSelectedListener)
    override fun getItemCount() = deviceList.size
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) = holder.bind(deviceList[position])

    class DeviceViewHolder(val item: View, private val mDeviceSelectedListener: DeviceSelectedListener) : RecyclerView.ViewHolder(item) {
        fun bind(device: Sensor) = with(item) {
            setOnClickListener { mDeviceSelectedListener.onDeviceSelected(it, adapterPosition) }
            device
                    .statusObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        findViewById<TextView>(R.id.item_status).text = it.name
                    }
            findViewById<TextView>(R.id.name_tv).text = device.name
            findViewById<TextView>(R.id.address_tv).text = device.address
        }
    }
}

interface DeviceSelectedListener {
    fun onDeviceSelected(v: View, position: Int)
}
