package me.cyber.nukleos.ui

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.synaps.UsbHandler
import me.cyber.nukleos.ui.charts.ChartsFragment
import me.cyber.nukleos.ui.control.SensorControlFragment
import me.cyber.nukleos.ui.find.FindSensorFragment
import me.cyber.nukleos.ui.predict.PredictFragment
import me.cyber.nukleus.R
import javax.inject.Inject
import me.cyber.nukleos.synaps.UsbService
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var peripheryManager: PeripheryManager

    private var navigationBlocked = false

    private var usbService: UsbService? = null
    var usbStreamReady: Boolean = false

    private val usbConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, arg1: IBinder) {
            usbStreamReady = false
            peripheryManager.synapsUsbHandler = UsbHandler(this@MainActivity)
            usbService = (arg1 as UsbService.UsbBinder).service
            usbService?.setHandler(peripheryManager.synapsUsbHandler)
            //
            Thread {
                usbService?.write("v".toByteArray())
                sleep(500)
                usbService?.write("~4".toByteArray())
                sleep(500)
                usbService?.write("b".toByteArray())
            }.start()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            usbService = null
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }


    /*
     * Notifications from UsbService will be received here.
     */
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getAction()) {
                UsbService.ACTION_USB_PERMISSION_GRANTED // USB PERMISSION GRANTED
                -> Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show()
                UsbService.ACTION_USB_PERMISSION_NOT_GRANTED // USB PERMISSION NOT GRANTED
                -> Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show()
                UsbService.ACTION_NO_USB // NO USB CONNECTED
                -> Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show()
                UsbService.ACTION_USB_DISCONNECTED // USB AVAILABLE
                -> Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show()
                UsbService.ACTION_USB_NOT_SUPPORTED // USB NOT SUPPORTED
                -> Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.new_toolbar))

        val fragmentList = listOf<Fragment>(
                FindSensorFragment.newInstance(),
                SensorControlFragment.newInstance(),
                ChartsFragment.newInstance(),
                PredictFragment.newInstance()
        )

        view_pager.adapter = MyAdapter(supportFragmentManager, fragmentList)
        view_pager.offscreenPageLimit = 3
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var prevMenuItem: MenuItem? = null
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem?.isChecked = false
                } else {
                    bottom_navigation.menu.getItem(0).isChecked = false
                }
                bottom_navigation.menu.getItem(position).isChecked = true
                prevMenuItem = bottom_navigation.menu.getItem(position)
            }

        })
        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            if (!navigationBlocked) {
                when (item.itemId) {
                    R.id.item_scan -> view_pager.currentItem = 0
                    R.id.item_control -> view_pager.currentItem = 1
                    R.id.item_graph -> view_pager.currentItem = 2
                    R.id.item_predict -> view_pager.currentItem = 3
                }
            }
            false
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    public override fun onResume() {
        super.onResume()
        setFilters()  // Start listening notifications from UsbService
        startService(UsbService::class.java, usbConnection, null) // Start UsbService(if it was not started before) and Bind it
    }


    private fun startService(service: Class<*>, serviceConnection: ServiceConnection, extras: Bundle?) {
        if (!UsbService.SERVICE_CONNECTED) {
            val startService = Intent(this, service)
            if (extras != null && !extras.isEmpty) {
                val keys = extras.keySet()
                for (key in keys) {
                    val extra = extras.getString(key)
                    startService.putExtra(key, extra)
                }
            }
            startService(startService)
        }
        val bindingIntent = Intent(this, service)
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun setFilters() {
        val filter = IntentFilter()
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED)
        filter.addAction(UsbService.ACTION_NO_USB)
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED)
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED)
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)
        registerReceiver(usbReceiver, filter)
    }

    public override fun onPause() {
        super.onPause()
        unregisterReceiver(usbReceiver)
        unbindService(usbConnection)
        peripheryManager.synapsUsbHandler = null
        usbStreamReady = false
    }

    fun navigateToPage(pageId: Int) {
        view_pager.currentItem = pageId
    }

    fun blockNavigaion(blocked: Boolean) {
        navigationBlocked = blocked
        view_pager.pagingEnabled = !blocked
    }

    inner class MyAdapter(fm: FragmentManager, private val fragmentList: List<Fragment>) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }
    }

}