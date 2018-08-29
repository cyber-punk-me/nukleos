package me.cyber.nukleos

import android.app.Activity
import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import me.cyber.nukleos.dagger.ApplicationComponent
import me.cyber.nukleos.dagger.ContextModule
import me.cyber.nukleos.dagger.DaggerApplicationComponent
import javax.inject.Inject

class App : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>


    companion object {
        @JvmStatic lateinit var applicationComponent : ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent
                .builder()
                .contextModule(ContextModule(applicationContext))
                .build()

        applicationComponent.inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingAndroidInjector
    }
}

