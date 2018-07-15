package me.cyber.nukleos.di.components

import android.content.Context
import dagger.Component
import me.cyber.nukleos.api.ApiHelper
import me.cyber.nukleos.di.modules.ApiModule
import me.cyber.nukleos.di.modules.AppModule
import javax.inject.Singleton


/**
 * Component for working with all app's activities
 */

@Component(modules = arrayOf(
        AppModule::class,
        ApiModule::class))

@Singleton
interface AppComponent {
    fun getAppContext(): Context
    fun getApiHelper(): ApiHelper
}