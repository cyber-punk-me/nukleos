package me.cyber.nukleos.di.components

import android.content.Context
import dagger.Component
import me.cyber.nukleos.api.ApiHelper
import me.cyber.nukleos.di.modules.ApiModule
import me.cyber.nukleos.di.modules.AppModule
import me.cyber.nukleos.di.modules.NavigationModule
import me.cyber.nukleos.di.modules.SensorModule
import me.cyber.nukleos.navigation.INavigator
import me.cyber.nukleos.sensor.ISensor
import javax.inject.Singleton


/**
 * Component for working with all app's activities
 */

@Component(modules = arrayOf(
        AppModule::class,
        ApiModule::class,
        NavigationModule::class,
        SensorModule::class))

@Singleton
interface AppComponent {
    fun getAppContext(): Context
    fun getApiHelper(): ApiHelper
    fun navigator(): INavigator
    fun sensor(): ISensor
}