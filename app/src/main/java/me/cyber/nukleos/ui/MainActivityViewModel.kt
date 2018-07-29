package me.cyber.nukleos.ui

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.design.widget.BottomNavigationView
import android.view.MenuItem
import android.view.View
import me.cyber.nukleos.App
import me.cyber.nukleos.R
import me.cyber.nukleos.extensions.getString

class MainActivityViewModel : BottomNavigationView.OnNavigationItemSelectedListener {

    val startItemId = ObservableInt(R.id.dashboard)
    val toolbarTitle = ObservableField<String>("toolbarTitle")
    override fun onNavigationItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.dashboard -> {
                    App.appComponent.navigator().showDashboard()
                    toolbarTitle.set(R.string.dashboard.getString())
                    true
                }

                R.id.learning -> {
                    App.appComponent.navigator().showLearningPage()
                    toolbarTitle.set(R.string.learning.getString())
                    true
                }

                R.id.about ->{
                    toolbarTitle.set(R.string.learning.getString())
                    true
                }

                else -> false
            }
}