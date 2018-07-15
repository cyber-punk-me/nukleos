package me.cyber.nukleos

import android.app.Application
import android.content.Context
import me.cyber.nukleos.di.components.AppComponent
import io.kyr.jarvis.di.components.DaggerAppComponent
import me.cyber.nukleos.di.modules.AppModule

class App : Application() {

    companion object {
        lateinit var appComponent: AppComponent
    }

    private lateinit var mContext: Context

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(mContext))
                .build()
    }
}

