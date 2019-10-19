package me.cyber.nukleos.ui.control

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_sensor_control.*
import me.cyber.nukleos.BaseFragment
import me.cyber.nukleos.utils.showShortToast
import me.cyber.nukleus.R
import javax.inject.Inject


class PeripheryControlFragment : BaseFragment<PeripheryControlInterface.Presenter>(), PeripheryControlInterface.View {

    companion object {
        fun newInstance() = PeripheryControlFragment()
    }

    @Inject
    lateinit var controlDevicePresenter: PeripheryStuffPresenter

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this as Fragment)
        attachPresenter(controlDevicePresenter)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_sensor_control, container, false).apply { setHasOptionsMenu(true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(controlDevicePresenter) {
            vibro_button_1.setOnClickListener { onVibrationClicked(1) }
            vibro_button_2.setOnClickListener { onVibrationClicked(2) }
            vibro_button_3.setOnClickListener { onVibrationClicked(3) }
            button_connect.setOnClickListener { onConnectSensorClicked() }
            button_connect_motors.setOnClickListener{ onConnectMotorsClicked() }
            sensor_frequency_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { onProgressSelected(progress) }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
            sensor_frequency_seekbar.isEnabled = false
        }
    }
    override fun showSensorStuffInformation(name: String?, address: String) {
        device_name.text = name ?: getString(R.string.unknown_stuff)
        device_address.text = address
    }

    override fun showConnectionLoader() {
        connection_loader.animate().alpha(1.0f)
    }

    override fun hideConnectionLoader() {
        connection_loader.animate().alpha(0.0f)
    }

    override fun showSensorConnecting() {
        device_status.text = getString(R.string.connecting)
    }

    override fun showSensorConnected() {
        device_status.text = getString(R.string.connected)
        button_connect.text = getString(R.string.disconnect)
    }

    override fun showSensorDisconnected() {
        device_status.text = getString(R.string.disconnected)
        button_connect.text = getString(R.string.connect)
    }

    override fun showSensorConnectionError() {
        "Connection failed".showShortToast()
        button_connect.text = getString(R.string.connect)
    }

    override fun enableSensorConnectButton() {
        button_connect.isEnabled = true
    }

    override fun disableSensorConnectButton() {
        button_connect.isEnabled = false
    }

    override fun disableSensorControlPanel() {
        vibro_button_1.isEnabled = false
        vibro_button_2.isEnabled = false
        vibro_button_3.isEnabled = false
        sensor_frequency_seekbar.isEnabled = false
    }

    override fun enableSensorControlPanel() {
        vibro_button_1.isEnabled = true
        vibro_button_2.isEnabled = true
        vibro_button_3.isEnabled = true
        sensor_frequency_seekbar.isEnabled = true
    }

    override fun showSensorStreaming() {
        device_status?.text = getString(R.string.currently_streaming)
    }

    override fun showSensorNotStreaming() {
        device_status?.text = getString(R.string.disconnected)
    }

    override fun showSensorScanFrequency(frequency: Int) {
        device_frequency_value.text = getString(R.string.hz_step, frequency)
    }

    override fun showMotorsConnected() {
        motor_status?.text = getString(R.string.connected)
        button_connect_motors.text = getString(R.string.disconnect)
        button_connect_motors.isEnabled = true
    }

    override fun showMotorsConnecting() {
        motor_status?.text = getString(R.string.connecting)
        button_connect_motors.isEnabled = false
    }

    override fun showMotorsDisonnected() {
        motor_status?.text = getString(R.string.disconnected)
        button_connect_motors.text = getString(R.string.connect)
        button_connect_motors.isEnabled = true
    }
}