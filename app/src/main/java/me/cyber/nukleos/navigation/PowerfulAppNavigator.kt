package me.cyber.nukleos.navigation

import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Forward
import ru.terrakok.cicerone.commands.Replace

abstract class PowerfulAppNavigator(val fragmentActivity: FragmentActivity, val fragmentContainerResId: Int)
    : SupportAppNavigator(fragmentActivity, fragmentContainerResId) {

    override fun forward(command: Forward) {
        val fragmentManager = fragmentActivity.supportFragmentManager
        val fragment = createDialogFragment(command.screenKey, command.transitionData)
        if (fragment != null && fragmentManager.findFragmentByTag(command.screenKey) == null) {
            fragment.show(fragmentManager, command.screenKey)
        } else {
            super.forward(command)
        }
    }

    // переделано. добавлен параметр тэг в метод реплэйса фрагментов
    override fun replace(command: Replace?) {
        val fragmentManager = fragmentActivity.supportFragmentManager
        val screenKey = command?.screenKey
        val fragment = createFragment(screenKey, command?.transitionData)
        if (fragment != null) {
            val fragmentTransaction = fragmentManager.beginTransaction()
            setupFragmentTransactionAnimation(
                    command,
                    fragmentManager.findFragmentById(fragmentContainerResId),
                    fragment,
                    fragmentTransaction
            )
            if (localStackCopy.size > 0) {
                fragmentManager?.popBackStack()
                localStackCopy.pop()
                fragmentTransaction
                        .replace(fragmentContainerResId, fragment, screenKey)
                        .addToBackStack(screenKey)
                        .commit()
                localStackCopy.add(screenKey)
            } else {
                fragmentTransaction
                        .replace(fragmentContainerResId, fragment, screenKey)
                        .commit()
            }
        } else {
            super.replace(command)
        }
    }

    protected abstract fun createDialogFragment(screenKey: String, data: Any?): DialogFragment?

}