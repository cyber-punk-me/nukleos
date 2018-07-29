package me.cyber.nukleos.navigation

import android.content.Context
import android.content.Intent
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import me.cyber.nukleos.navigation.Screens.ABOUT_PAGE
import me.cyber.nukleos.navigation.Screens.DASHBOARD_PAGE
import me.cyber.nukleos.navigation.Screens.LEARNING_PAGE
import me.cyber.nukleos.ui.LearningPage
import me.cyber.nukleos.ui.dashboard.DashboardPage
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.result.ResultListener

/**
 * Mission - navigate over app screens
 */

interface INavigator {
    fun showDashboard()
    fun showLearningPage()
    fun showAboutPage()
    fun updateNavigationContext(context: INavigationContext?)
    fun setResultListener(resultCode: Int, listener: ResultListener)
    fun removeResultListener(resultCode: Int)
    fun setResult(requestCode: Int, data: Any)
}

interface IInnerNavigator

interface INavigationContext {
    val innerNavigator: IInnerNavigator
    val fragmentActivity: FragmentActivity
    val fragmentContainerResId: Int
}

/**
 * Hides navigation details, like cicerone usage
 */
class Navigator : INavigator {

    override fun showDashboard() = mCicerone.router.replaceScreen(DASHBOARD_PAGE)

    override fun showLearningPage() = mCicerone.router.replaceScreen(LEARNING_PAGE)

    override fun showAboutPage() = mCicerone.router.replaceScreen(ABOUT_PAGE)


    private class CiceroneNavigator(fragmentActivity: FragmentActivity, fragmentContainerResId: Int)
        : PowerfulAppNavigator(fragmentActivity, fragmentContainerResId), IInnerNavigator {

        override fun createDialogFragment(screenKey: String, data: Any?): DialogFragment? = when (screenKey) {
//            DIALOG_MENU -> (data as? ItemInfo)?.let {
//                DialogMenuFragment.newInstance(it)
//            }
            else -> null
        }

        override fun createActivityIntent(context: Context?, screenKey: String?, data: Any?): Intent? = when (screenKey) {
//            NAVIGATION -> BottomNavigationActivity.createIntent()
            else -> null
        }

        override fun createFragment(screenKey: String?, data: Any?) = when (screenKey) {
            DASHBOARD_PAGE -> DashboardPage.newInstance()
            LEARNING_PAGE -> LearningPage.newInstance()
            ABOUT_PAGE -> null
            else -> null
        }
    }

    companion object {
        fun createInnerNavigator(context: INavigationContext): IInnerNavigator =
                CiceroneNavigator(context.fragmentActivity, context.fragmentContainerResId)
    }

    private val mCicerone by lazy {
        Cicerone.create()
    }

    override fun updateNavigationContext(context: INavigationContext?) = with(mCicerone.navigatorHolder) {
        if (context == null) {
            removeNavigator()
        } else {
            setNavigator(context.innerNavigator as? Navigator)
        }
    }

    override fun setResultListener(resultCode: Int, listener: ResultListener) {
        mCicerone.router.setResultListener(resultCode, listener)
    }

    override fun removeResultListener(resultCode: Int) {
        mCicerone.router.removeResultListener(resultCode)
    }

    override fun setResult(requestCode: Int, data: Any) = mCicerone.router.exitWithResult(requestCode, data)
}