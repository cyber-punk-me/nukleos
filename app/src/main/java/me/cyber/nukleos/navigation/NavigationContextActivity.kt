package me.cyber.nukleos.navigation

import android.support.v4.app.FragmentActivity
import me.cyber.nukleos.App
import me.cyber.nukleos.R
import me.cyber.nukleos.ui.CrashReportActivity

/**
 * Holds navigation specified things
 */
open class NavigationContextActivity : CrashReportActivity(), INavigationContext {
    override val innerNavigator by lazy {
        Navigator.createInnerNavigator(this)
    }

    override val fragmentActivity: FragmentActivity
        get() = this

    override val fragmentContainerResId: Int
        get() = R.id.container

    override fun onResumeFragments() {
        super.onResumeFragments()
        App.appComponent.navigator().updateNavigationContext(this)
    }

    override fun onPause() {
        super.onPause()
        App.appComponent.navigator().updateNavigationContext(null)
    }
}